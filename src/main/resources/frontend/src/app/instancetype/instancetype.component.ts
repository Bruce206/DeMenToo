import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {InstanceTypeService} from "./instancetype.service";

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
