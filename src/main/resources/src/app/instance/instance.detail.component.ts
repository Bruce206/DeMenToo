import {Component, OnInit} from '@angular/core';
import 'rxjs/add/operator/map';
import {InstanceService} from "./instance.service";
import {ActivatedRoute} from "@angular/router";
import { PipeTransform, Pipe } from '@angular/core';


@Component({
  selector: 'instanceDetail',
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

  delete(): void {
    this.instanceService.delete(this.instance).subscribe();
    // window.close();
  }
}

@Pipe({name: 'keys'})
export class KeysPipe implements PipeTransform {
  transform(value, args: string[]): any {
    let keys = [];
    for (let key in value) {
      keys.push({key: key, value: value[key]});
    }
    return keys;
  }
}
