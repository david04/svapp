package com.github.david04.svapp.rengine

import java.io.{FileOutputStream, FileInputStream, File}
import org.apache.poi.hssf.usermodel.{HSSFSheet, HSSFWorkbook}
import org.apache.poi.ss.usermodel.Cell
import org.apache.log4j.Logger
import collection.JavaConversions._
import com.github.david04.svapp.util.Execute

trait Table[T] {
  val name: String
  val data: Seq[T]
  val fields: Map[String, T => Any]

  def field(name: String, idx: Int) = if (idx < data.size) Some(fields(name)(data(idx))) else None
}

abstract class Report(template: File, output: File) {

  val fields: Map[String, Any]

  val tables: Map[String, Table[_]]

  def fill = {
    val in = new FileInputStream(template)
    val book = new HSSFWorkbook(in)
    in.close

    def cannonic(s: String) = s.replaceAll("[-._\\{\\}\\$ ]", "").toLowerCase

    def replaceFields(fields: Map[String, Any])(c: Cell) {
      val pattern = "(\\$\\w[-_\\w.]*|\\$\\{\\w[-_\\w. ]*\\})".r.pattern
      c.getCellType match {
        case Cell.CELL_TYPE_STRING => {
          val initial = c.getStringCellValue
          val matcher = pattern.matcher(initial)
          val sb = new StringBuffer()
          while (matcher.find()) {
            val field = matcher.group()
            val cannonic = field.replaceAll("[-._\\{\\}\\$ ]", "").toLowerCase
            if (!fields.contains(cannonic)) Logger.getLogger(this.getClass).error("Unknown field: " + field)
            val value = fields.getOrElse(cannonic, "").toString
            matcher.appendReplacement(sb, value)
          }
          matcher.appendTail(sb)
          if (initial != sb.toString) c.setCellValue(sb.toString)
        }
        case _ =>
      }
    }

    val tregex = "\\$\\[([-_\\w. ]+)\\]\\[([-_\\w. ]+)\\]"
    val tpattern = tregex.r
    def findTableEntries(c: Cell): List[(String, String)] =
      if (c.getCellType == Cell.CELL_TYPE_STRING)
        tpattern.findAllIn(c.getStringCellValue).toList.map(_ match {
          case tpattern(table, field) => (table, field)
        })
      else List()

    def replaceTableFields(table: Table[_], row: Int)(c: Cell) {
      c.getCellType match {
        case Cell.CELL_TYPE_STRING => {
          val initial = c.getStringCellValue
          if (!initial.matches(tregex)) {
            // Pattern in in the middle of some text
            val matcher = tpattern.pattern.matcher(initial)
            val sb = new StringBuffer()
            var hasData = true
            while (hasData && matcher.find()) {
              tpattern.findAllIn(matcher.group()).map(_ match {
                case tpattern(table, field) => tables(cannonic(table)).field(cannonic(field), row)
              }).next() match {
                case Some(v) => matcher.appendReplacement(sb, v.toString)
                case None => hasData = false
              }
            }
            matcher.appendTail(sb)
            if (hasData) {
              if (initial != sb.toString) c.setCellValue(sb.toString)
            } else c.setCellValue("")
          } else {
            // Pattern fills the whole cell
            tpattern.findAllIn(initial).map(_ match {
              case tpattern(table, field) => tables(cannonic(table)).field(cannonic(field), row)
            }).next() match {
              case Some(i: Int) => c.setCellValue(i)
              case Some(d: Double) => c.setCellValue(d)
              case Some(s: String) => c.setCellValue(s)
              case None => c.setCellValue("")
            }
          }
        }
        case _ =>
      }
    }

    val nOriginalSheets = book.getNumberOfSheets

    val sheets =
      (0 to (book.getNumberOfSheets - 1))
        .foreach(sIdx => {

        val origSheet = book.getSheetAt(sIdx)

        val tnrows = tables.map(t =>
          (t._2, {
            val rows = (0 to origSheet.getLastRowNum).map(i => (i, origSheet.getRow(i))).filter(_._2 != null).filter(_._2.cellIterator().exists(c => !findTableEntries(c).filter(_._1 == t._1).isEmpty)).toList
            if (rows.size == 1) {
              val row = rows.head
              (1 to t._2.data.size - 1).foreach(_ => copyRow(origSheet, row._1, row._1 + 1))
              t._2.data.size
            } else {
              rows.size
            }
          }))
          .filter(_._2 > 0)

        val npages = if (tnrows.isEmpty) 1 else math.max(1, tnrows.map(nrows => (math.ceil(nrows._1.data.size.toDouble / nrows._2)).toInt).max)

        (0 to npages - 1).map(page => {
          val s = book.cloneSheet(sIdx)
          book.setSheetName(book.getSheetIndex(s.getSheetName), origSheet.getSheetName + s"_${page + 1}")

          s.rowIterator.foreach(r =>
            r.cellIterator().foreach(replaceFields(fields ++ Map("page" -> s"${page + 1}", "npages" -> s"$npages"))))

          tnrows.foreach(_ match {
            case (t, nrows) =>
              s.rowIterator().filter(_.cellIterator().exists(c => !findTableEntries(c).filter(_._1 == t.name).isEmpty))
                .zipWithIndex.foreach(r => r._1.cellIterator().foreach(replaceTableFields(t, nrows * page + r._2)))
          })
        })
      })

    (1 to nOriginalSheets).foreach(_ => book.removeSheetAt(0))

    //        val fname = template.getParentFile.getAbsolutePath + File.separator + template.getName.replaceAll(".xls$", s"_${customer.id}.xls")
    val out = new FileOutputStream(output)
    book.write(out)
    out.close

    this
  }

  def toPDF(): File = {
    val exec = s"soffice.bin --headless --convert-to pdf '${output.getAbsolutePath}' -outdir '${output.getAbsoluteFile.getParent}'"
    Execute(exec)
    new File(output.getAbsolutePath.replaceFirst("xls$", "pdf"))
  }

  private def copyRow(worksheet: HSSFSheet, sourceRowNum: Int, destinationRowNum: Int) {
    var newRow = worksheet.getRow(destinationRowNum)
    val sourceRow = worksheet.getRow(sourceRowNum)

    if (newRow != null) worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum, 1)
    else newRow = worksheet.createRow(destinationRowNum)

    (0 to (sourceRow.getLastCellNum - 1)).foreach(i => {

      val oldCell = sourceRow.getCell(i)
      var newCell = newRow.createCell(i)

      if (oldCell != null) {
        newCell.setCellStyle(oldCell.getCellStyle)
        if (newCell.getCellComment != null) newCell.setCellComment(oldCell.getCellComment)

        if (oldCell.getHyperlink != null) newCell.setHyperlink(oldCell.getHyperlink)

        newCell.setCellType(oldCell.getCellType)

        oldCell.getCellType match {
          case Cell.CELL_TYPE_BLANK =>
          case Cell.CELL_TYPE_BOOLEAN => newCell.setCellValue(oldCell.getBooleanCellValue)
          case Cell.CELL_TYPE_ERROR => newCell.setCellErrorValue(oldCell.getErrorCellValue)
          case Cell.CELL_TYPE_FORMULA => newCell.setCellFormula(oldCell.getCellFormula)
          case Cell.CELL_TYPE_NUMERIC => newCell.setCellValue(oldCell.getNumericCellValue)
          case Cell.CELL_TYPE_STRING => newCell.setCellValue(oldCell.getRichStringCellValue)
        }
      }
    })
  }
}