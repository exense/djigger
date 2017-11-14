# Setup Djigger Startup on Linux

This directory contains a number of helper scripts to properly integrate the
Djigger Collector and Client startup on a server or a desktop environment.

## djigger Collector

If you want to automatically startup the djigger collector service on system
boot it has to be integrated into the init-system. On traditional Linux
distributions such as RHEL 6/CentOS 6 follow the SYSV init approach. More
modern distributions such as RHEL 7/CentOS 7 and Ubuntu 16.04 are using the
`systemd` init system for which the corresponding instructions below should
be used. 

### systemd

The only file required for the `systemd` setup is the
`contrib/djigger-collector.service` is an example for a systemd
[service unit](https://www.freedesktop.org/software/systemd/man/systemd.service.html).
`systemd` allows a service to be setup and controlled in a user session what
might be useful on a developer machine. In a production setup where only one
collector is required which is then managed a with privileged admin account,
the collector service can be setup as regular `systemd` service.

#### Production Setup

Setup the collector daemon as regular `systemd` service. The following tasks
prefixed with a `#` must be done with root permissions. Copy the service file
into the system configuration directory and change the ownership to root:

    # cp contrib/djigger-collector.service /etc/systemd/system/djigger-collector.service
    # chown root:root /etc/systemd/system/djigger-collector.service
    # chmod 0644 /etc/systemd/system/djigger-collector.service

**Service Configuration**

Adjust the content of `/etc/systemd/system/djigger-collector.service` according
to your djigger setup:

* `Exec` (defaults to `/opt/denkbar/djigger/bin/startCollector.sh`): Command
  which is executed when starting the service. Adjust this path to your djigger
  installation.
* `User`/`Group` (defaults to `djigger`): User account which will run the
  collector service. You might need to create the user account and give it
  access to the djigger installation. E.g.

      # useradd -r -d /opt/denkbar/djigger djigger
      # chown -R djigger:djigger /opt/denkbar/djigger

* `Environment` (unset by default): Set any environment variable for the
  collector service. E.g. you must define `JAVA_HOME` if you want to use the
  direct process attach feature of the collector service as following:

      Environment=JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk

  Optionally additional environment variables accepted by the collector startup
  script can be added. E.g. to define additional startup arguments, you can
  set:

      Environment=JAVA_OPTS=-Xmx512m

**Autostart**

To enable autostart on system boot, enable the service via `systemctl`:

    # systemctl enable djigger-collector

**Start/Stop Service**

To start/stop/restart the collector service run:

    # systemctl start djigger-collector
    # systemctl stop djigger-collector
    # systemctl restart djigger-collector

Check the status of the collector service:

    # systemctl status djigger-collector

### SYSV Init

**Service Configuration**

Copy the service configuration file to the `/etc/sysconfig` directory and
change its ownership to root:

    # cp contrib/djigger-collector.sysconfig /etc/sysconfig/djigger-collector
    # chown root:root /etc/sysconfig/djigger-collector
    # chmod 0644 /etc/sysconfig/djigger-collector

In this configuration file, you can set various variables which influence the
service startup:

* `DJIGGER_HOME` (defaults to `/opt/denkbar/djigger`) Path to your djigger
  installation.
* `DJIGGER_USER` (defaults to `djigger`): User account which will run the
  collector service. You might need to create the user account and give it
  access to the djigger installation. E.g.
  
      # useradd -r -d /opt/denkbar/djigger djigger
      # chown -R djigger:djigger /opt/denkbar/djigger
  
* `DJIGGER_OUTFILE` (defaults to `/opt/denkbar/djigger/log/collector.out`):
  Logfile where stdout and stderr of the collector process are stored. Set
  this to `/dev/null` if those messages shouldn't be stored.
  
* `JAVA_HOME` (unset by default): Root directory of your Java Development Kit
  installation. You must set this if you want to use the direct process attach
  feature of the collector service.
  
* `JAVA_OPTS` (unset by default): Custom Java VM startup arguments. Here you
  can e.g. restrict the heap size via `-Xmx512m`
  
**Init Script**

Copy the init script to the `/etc/init.d` directory, change its ownership to
root and make it executable:

     # cp contrib/djigger-collector.initd /etc/init.d/djigger-collector
     # chown root:root /etc/init.d/djigger-collector
     # chmod 0755 /etc/init.d/djigger-collector
     
To enable autostart on system boot, register the service via `chkconfig`:

    # chkconfig djigger-collector on
    
To manually start/stop/restart the collector service run:

    # service djigger-collector start
    # service djigger-collector stop
    # service djigger-collector restart

Check the status of the collector service:

    # service djigger-collector status
