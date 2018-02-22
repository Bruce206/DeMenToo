import {Component, OnInit, ViewEncapsulation, OnDestroy} from '@angular/core';
import 'rxjs/add/operator/map';
import {ActivatedRoute} from "@angular/router";
import {SelectItem} from "primeng/primeng";
import {InstanceTypeService} from "./instancetype.service";
import {SortPipe} from "../sort.pipe";
import {StompService} from 'ng2-stomp-service';
import {InstanceService} from "../instance/instance.service";


@Component({
  selector: 'instancetype',
  templateUrl: 'instancetype.component.html',
  styleUrls: ['../instance/instance.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class InstanceTypeComponent implements OnInit {
  public instanceType: any;
  changeFile: boolean = false;

  constructor(private instanceTypeService: InstanceTypeService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.fetchInstanceType();
  }

  fetchInstanceType() {
    this.route.params.subscribe(params => {
      this.instanceTypeService.get(params['type']).subscribe((data) => {
        this.instanceType = data;
      });
    });
  }

  save() {
    this.instanceTypeService.save(this.instanceType).subscribe(it => {
      this.instanceType = it;
    });
  }

  onUploadFinished($event) {
    this.changeFile = false;
    this.fetchInstanceType();
  }
}
