### Ansible playbooks to install kafka cluster and zookeeper ensemble

```
PLAY [CHECK ZOOKEEPER CLUSTER SERVICES STATUS] ********************************************************************************************************************************************************************

TASK [get service facts] ******************************************************************************************************************************************************************************************
ok: [10.72.5.112]
ok: [10.72.5.111]
ok: [10.72.5.110]

TASK [check zookeeper status] *************************************************************************************************************************************************************************************
ok: [10.72.5.110] => {
    "ansible_facts.services[\"zookeeper.service\"]": {
        "name": "zookeeper.service",
        "source": "systemd",
        "state": "running",
        "status": "enabled"
    }
}
ok: [10.72.5.111] => {
    "ansible_facts.services[\"zookeeper.service\"]": {
        "name": "zookeeper.service",
        "source": "systemd",
        "state": "running",
        "status": "enabled"
    }
}
ok: [10.72.5.112] => {
    "ansible_facts.services[\"zookeeper.service\"]": {
        "name": "zookeeper.service",
        "source": "systemd",
        "state": "running",
        "status": "enabled"
    }
}

PLAY [CHECK KAFKA CLUSTER SERVICES STATUS] ************************************************************************************************************************************************************************

TASK [get service facts] ******************************************************************************************************************************************************************************************
ok: [10.72.5.111]
ok: [10.72.5.110]
ok: [10.72.5.112]

TASK [check kafka status] *****************************************************************************************************************************************************************************************
ok: [10.72.5.110] => {
    "ansible_facts.services[\"kafka.service\"]": {
        "name": "kafka.service",
        "source": "systemd",
        "state": "running",
        "status": "enabled"
    }
}
ok: [10.72.5.111] => {
    "ansible_facts.services[\"kafka.service\"]": {
        "name": "kafka.service",
        "source": "systemd",
        "state": "running",
        "status": "enabled"
    }
}
ok: [10.72.5.112] => {
    "ansible_facts.services[\"kafka.service\"]": {
        "name": "kafka.service",
        "source": "systemd",
        "state": "running",
        "status": "enabled"
    }
}

PLAY RECAP ********************************************************************************************************************************************************************************************************
10.72.5.110                : ok=4    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
10.72.5.111                : ok=4    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
10.72.5.112                : ok=4    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
```

### TODO: remove bashsible

---

### References

- [Kafka documentation](https://kafka.apache.org/documentation/), official kafka documentation
- [Apache Zookeeper](https://zookeeper.apache.org/), Apache Zookeeper
- [Yahoo/CMAK](https://github.com/yahoo/CMAK), Yahoo CMAK project
- [Ansible Documentations](https://docs.ansible.com/), list of documentations for ansible
