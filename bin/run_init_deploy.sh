
source settings.sh

#RUN sudo yum -y install postgresql postgresql-server

#TODO: TRUST LOCAL USERS @ POSTGRES
#RUN sudo echo bash -c \\\"echo -e \\\\\\"# local is for Unix domain socket connections only\nlocal   all             all                                     trust\n# IPv4 local connections:\nhost    all             all             127.0.0.1/32            trust\n# IPv6 local connections:\nhost    all             all             ::1/128                 trust\\\\\\" \\\"
# /var/lib/pgsql9/data/pg_hba.conf

#RUN "sudo chkconfig postgresql on"

#RUN "sudo service postgresql initdb"
#RUN "sudo service postgresql start"

#RUN "sudo sudo -u postgres psql -c \"CREATE USER $NAME WITH PASSWORD '$NAME';\""
#RUN "sudo sudo -u postgres psql -c 'ALTER ROLE $NAME CREATEDB;'"
#RUN "sudo sudo -u postgres psql -c 'CREATE DATABASE $NAME;'"

#RUN "sudo mkdir -p /opt; sudo chown ec2-user:root /opt; sudo chmod 700 /opt"

#RUN "cd /opt ; wget -v '$JETTY_URL'; tar -xzf '$JETTY_VER.tar.gz'"
#RUN "cd /opt/$JETTY_VER ; rm -rfv contexts/* ; rm -rfv webapps/*war "

#TODO: SET JETTY PORT at etc/jetty.xml

#./run_deploy.sh



