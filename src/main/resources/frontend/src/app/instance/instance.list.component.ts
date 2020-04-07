import {Component, OnInit, ViewEncapsulation, OnDestroy, ViewChild} from '@angular/core';
import {InstanceService} from "./instance.service";
import { Table } from 'primeng/table';
import {SelectItem} from "primeng/api";
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

@Component({
  selector: 'instance',
  templateUrl: 'instance.list.component.html',
  styleUrls: ['instance.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class InstanceListComponent implements OnInit, OnDestroy {
  @ViewChild(('dt')) dt: Table;

  ngOnDestroy(): void {
    this.subscription.disconnect();
  }

  public instances: any[] = [];
  public columnOptions: SelectItem[];
  public cols: any[];

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
    this.instanceService.getList().subscribe((data) => {
      this.instances = data;
    });

    this.cols = [
      {field: 'prod', header: 'Prod', filter: false, pos: 1, class: "col-icon"},
      {field: 'type', header: 'App-Typ', filter: true, pos: 2},
      {field: 'identifier', header: 'App-Name', filter: true, pos: 3},
      {field: 'domains', header: 'Domain', filter: true, pos: 4},
      {field: 'licensedFor', header: 'Kunde', filter: true, pos: 5},
      {field: 'version', header: 'Version', filter: true, pos: 6},
      {field: 'server', header: 'Server', filter: true, pos: 7},
      {field: 'port', header: 'Port', filter: true, pos: 8},
      {field: 'modified', header: 'Letzte Meldung', filter: false, pos: 9},
      {field: 'status', header: 'Status', filter: false, pos: 10},
      {field: 'responseTime', header: 'Responsetime', filter: false, pos: 11, class: "col-align-right"}
    ];

    this.columnOptions = [];
    for (let i = 0; i < this.cols.length; i++) {
      this.columnOptions.push({label: this.cols[i].header, value: this.cols[i]});
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
