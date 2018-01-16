import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import 'rxjs/add/operator/map';
import {ActivatedRoute} from "@angular/router";
import {SelectItem} from "primeng/primeng";
import {InstanceTypeService} from "./instancetype.service";
import {SortPipe} from "../sort.pipe";


@Component({
  selector: 'instancetype',
  templateUrl: 'instancetype.component.html',
  styleUrls: ['instance.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class InstanceTypeComponent implements OnInit {

  public instanceType: any = {instances: []};
  public columnOptions: SelectItem[];
  public cols: any[];
  constructor(private instanceTypeService: InstanceTypeService, private route: ActivatedRoute, private sortPipe: SortPipe) {
  }

  ngOnInit(): void {
    this.cols = [
      {field: 'prod', header: 'Prod', filter: false, pos: 1, class: "col-icon"},
      {field: 'type', header: 'App-Type', filter: true, pos: 2},
      {field: 'identifier', header: 'App-Name', filter: true, pos: 3},
      {field: 'domains', header: 'Domain', filter: true, pos: 4},
      {field: 'licensedFor', header: 'Customer', filter: true, pos: 5},
      {field: 'version', header: 'Version', filter: true, pos: 6},
      {field: 'modified', header: 'Last Message', filter: false, pos: 7}
    ];

    this.route.params.subscribe(params => {
      this.instanceTypeService.get(params['type']).subscribe((data) => {
        this.instanceType = data;
        if (data.instances && data.instances.length > 0) {
          for (let instance of data.instances) {
            for (let detail of instance.details) {
              data.instances[0][detail.key] = data.instances[0].instanceDetailsByKey[detail.key][0].value;
            }
          }

          let pos = 8;
          this.sortPipe.transform(data.instances[0].details, 'key');
          for (let detail of data.instances[0].details) {
            this.cols.push({field: detail.key, header: detail.key, filter: true, pos: pos++});
          }

          this.columnOptions = [];
          for (let i = 0; i < this.cols.length; i++) {
            this.columnOptions.push({label: this.cols[i].header, value: this.cols[i]});
          }
        }
      });
    });


  }

  handleRowClick(event: any) {
    window.open('/instance/detail/' + event.data.id, "_blank");
  }

}
