import {Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Table} from "primeng/table";
import {SelectItem} from "primeng/api";
import {InstanceService} from "../instance/instance.service";

import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';
import {Instance, Server} from "../java-types-module";

export interface InstanceImpl extends Instance {
  responseTime: any;
  status: string | number;
}

@Component({
  selector: 'app-instance-table',
  templateUrl: './instance-table.component.html',
  styleUrls: ['./instance-table.component.scss']
})
export class InstanceTableComponent implements OnInit, OnDestroy {

  @ViewChild(('dt')) dt: Table;

  @Input() public instances: InstanceImpl[] = [];

  @Input() public initialColumns: string[] = ['prod', 'type', 'identifier', 'domains', 'licensedFor', 'version', 'server',
    'port', 'modified', 'status', 'responseTime'];

  ngOnDestroy(): void {
    this.subscription.disconnect();
  }

  public columnOptions: SelectItem[];
  public cols: any[] = [];

  private subscription : any;

  constructor(private instanceService: InstanceService) {
    let ws = new SockJS("/socket");
    this.subscription = Stomp.over(ws);
    this.subscription.debug = null;
    let that = this;

    this.subscription.connect({}, function() {
      that.subscription.subscribe("/instancestatus", (data) => that.response(JSON.parse(data.body)));
    });
  }

  //response
  public response = (data) => {
    for (let i of this.instances) {
      if (i.id === data.instance.id) {
        console.log(data.instance);
        i.status = data.status;
        // i.lastMessage = data.instance.lastMessage;
        // i.timeAgo = data.instance.timeAgo;
        i.lastMessageCritical = data.instance.lastMessageCritical;
        i.responseTime = i.status === 'OK' ? data.responseTime : "";
      }
    }
  };

  ngOnInit(): void {
    let initialCols:any[] = [
      {field: 'prod', header: 'Prod', filter: false, class: "col-icon"},
      {field: 'type', header: 'App-Typ', filter: true},
      {field: 'identifier', header: 'App-Name', filter: true},
      {field: 'domains', header: 'Domain', filter: true},
      {field: 'licensedFor', header: 'Kunde', filter: true},
      {field: 'version', header: 'Version', filter: true},
      {field: 'server', header: 'Server', filter: true},
      {field: 'port', header: 'Port', filter: true},
      {field: 'modified', header: 'Letzte Meldung', filter: false},
      {field: 'status', header: 'Status', filter: false},
      {field: 'responseTime', header: 'Responsetime', filter: false, class: "col-align-right"}
    ];

    let pos:number = 0;
    for (let col of initialCols) {
      col.pos = pos++;
      if (this.initialColumns.includes(col.field)) {
        this.cols.push(col);
      }
    }

    this.columnOptions = [];
    for (let i = 0; i < initialCols.length; i++) {
      this.columnOptions.push({label: initialCols[i].header, value: initialCols[i]});
    }
  }

  lookupRowStyleClass(instance: any) {
    if (instance.excludeFromHealthcheck === true) {
      instance.status = "Excluded";
      return "excluded-from-healthcheck";
    }

    if (instance.status === undefined && instance.instanceType.healthUrl) {
      return "pending";
    }

    if (instance.lastMessageCritical || instance.status !== "OK") {
      if (instance.prod) {
        return "critical";
      } else {
        return "testcritical";
      }
    }

    return "ok";
  }

  getColorForResponseTime(responseTime: number) {
    if (responseTime < 150) {
      return "green";
    }

    if (responseTime >= 150 && responseTime < 300) {
      return "orange";
    }

    if (responseTime >= 300) {
      return "red";
    }
  }

  handleRowClick(instance: any) {
    window.open('/instance/detail/' + instance.id, "_blank");
  }

  refreshHealth() {
    for (let instance of this.instances) {
      delete instance.status;
    }
    this.instanceService.refreshHealth();
  }

}
