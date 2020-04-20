import {Component, OnInit, ViewEncapsulation, OnDestroy} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {SelectItem} from "primeng/api";
import {InstanceTypeService} from "./instancetype.service";
import {SortPipe} from "../sort.pipe";
import {InstanceService} from "../instance/instance.service";
import {Subscription} from "rxjs";
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';
import {Instance} from "../java-types-module";

@Component({
  selector: 'instancetypeinstances',
  templateUrl: 'instancetype.instances.component.html',
  styleUrls: ['../instance/instance.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class InstanceTypeInstancesComponent implements OnInit {
  public instances: Instance[] = [];
  public columnOptions: SelectItem[] = [];
  constructor(private instanceTypeService: InstanceTypeService, private route: ActivatedRoute, private sortPipe: SortPipe) {
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.instanceTypeService.get(params['type']).subscribe((data) => {
        this.instances = data.instances;
        if (data.instances && data.instances.length > 0) {
          for (let instance of data.instances) {
            for (let detail of instance.details) {
              instance[detail.key] = instance.instanceDetailsByKey[detail.key][0].value;
            }
          }

          let pos = 9;
          this.sortPipe.transform(data.instances[0].details, 'key');
          data.instanceDetailKeys.sort();
          for (let key of data.instanceDetailKeys) {
            this.columnOptions.push({label: key, value: {field: key, header: key, filter: true, pos: pos++}});
          }
        }
      });
    });
  }
}
