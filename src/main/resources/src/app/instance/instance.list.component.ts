import {Component, OnInit, Pipe, ViewEncapsulation} from '@angular/core';
import 'rxjs/add/operator/map';
import {Router} from "@angular/router";
import {InstanceService} from "./instance.service";
import {OverlayPanel, SelectItem} from "primeng/primeng";
import {forEach} from "@angular/router/src/utils/collection";


@Component({
  selector: 'instance',
  templateUrl: 'instance.list.component.html',
  styleUrls: ['instance.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class InstanceListComponent implements OnInit {

  public instances: any[] = [];
  public columnOptions: SelectItem[];
  public cols: any[];
  constructor(private instanceService: InstanceService, private router: Router) {
  }

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
      {field: 'modified', header: 'Last Message', filter: false, pos: 7}
    ];

    this.columnOptions = [];
    for (let i = 0; i < this.cols.length; i++) {
      this.columnOptions.push({label: this.cols[i].header, value: this.cols[i]});
    }
  }

  lookupRowStyleClass(instance: any) {
    if (instance.lastMessageCritical) {
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
