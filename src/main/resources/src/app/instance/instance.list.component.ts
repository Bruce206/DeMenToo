import {Component, OnInit, Pipe, ViewEncapsulation, OnDestroy} from '@angular/core';
import 'rxjs/add/operator/map';
import {Router} from "@angular/router";
import {InstanceService} from "./instance.service";
import {OverlayPanel, SelectItem} from "primeng/primeng";
import {StompService} from 'ng2-stomp-service';

@Component({
  selector: 'instance',
  templateUrl: 'instance.list.component.html',
  styleUrls: ['instance.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class InstanceListComponent implements OnInit, OnDestroy {
  ngOnDestroy(): void {
    this.subscription.unsubscribe();
    this.stomp.disconnect().then(() => console.log("Socket closd"));
  }

  public instances: any[] = [];
  public columnOptions: SelectItem[];
  public cols: any[];

  private subscription : any;

  constructor(private instanceService: InstanceService, private router: Router, private stomp: StompService) {
    let getUrl = window.location;
    let baseUrl = getUrl.protocol + "//" + getUrl.host;

    stomp.configure({
      host:baseUrl + '/socket',
      debug:false,
      queue:{'init':false}
    });

    stomp.startConnect().then(() => {
      stomp.done('init');
      this.subscription = stomp.subscribe('/instancestatus', this.response);
    });
  }

  //response
  public response = (data) => {
    for (let i of this.instances) {
      if (i.id === data.instance.id) {
        i.status = data.status;
        i.lastMessage = data.instance.lastMessage;
      }
    }
  };

  ngOnInit(): void {
    this.instanceService.getList().subscribe((data) => {
      this.instances = data;
    });

    this.cols = [
      {field: 'prod', header: 'Prod', filter: false, pos: 1, class: "col-icon"},
      {field: 'type', header: 'App-Type', filter: true, pos: 2},
      {field: 'identifier', header: 'App-Name', filter: true, pos: 3},
      {field: 'domains', header: 'Domain', filter: true, pos: 4},
      {field: 'licensedFor', header: 'Customer', filter: true, pos: 5},
      {field: 'version', header: 'Version', filter: true, pos: 6},
      {field: 'modified', header: 'Last Message', filter: false, pos: 7},
      {field: 'status', header: 'Status', filter: false, pos: 8}
    ];

    this.columnOptions = [];
    for (let i = 0; i < this.cols.length; i++) {
      this.columnOptions.push({label: this.cols[i].header, value: this.cols[i]});
    }
  }

  lookupRowStyleClass(instance: any) {
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
  }

  handleRowClick(event: any) {
    window.open('/instance/detail/' + event.data.id, "_blank");
  }

}
