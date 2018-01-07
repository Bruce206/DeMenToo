import {Component, OnInit} from '@angular/core';
import 'rxjs/add/operator/map';
import {InstanceService} from "./instance.service";
import {ActivatedRoute} from "@angular/router";


@Component({
  selector: 'instanceDet',
  templateUrl: 'instance.detail.component.html',
  styleUrls: ['instance.component.css']
})
export class InstanceDetailComponent implements OnInit {

  public instance: any;

  constructor(private instanceService: InstanceService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.instanceService.get(+params['id']).subscribe((data) => {
        this.instance = data;
      });
    });

  }

}
