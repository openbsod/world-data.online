# Deploying Cassandra cluster using Ansible

## Execution steps:
- Install ansible
- git clone https://github.com/
- cd cassandra-ansible
- run ansible-playbook ...


## Directory structure
``` sh
└── cassandra-cluster
    ├── handlers
    │   └── main.yml
    ├── tasks
    │   └── main.yml
    ├── templates
    │   ├── cassandra.sh
    │   └── cassandra.yaml.j2
    └── vars
        └── main.yml
```
