import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {InstanceService} from "./instance.service";
import {InstanceImpl} from "../instance-table/instance-table.component";

@Component({
  selector: 'instance',
  templateUrl: 'instance.list.component.html',
  styleUrls: ['instance.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class InstanceListComponent implements OnInit {
  public instances: InstanceImpl[] = [];

  constructor(private instanceService: InstanceService) {}

  ngOnInit(): void {
    this.instanceService.getList().subscribe((data) => {
      this.instances = data;
    });
  }

}
