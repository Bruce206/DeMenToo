import {NgModule} from '@angular/core';
import {CommonModule} from "@angular/common";
import {UiComponentsModule} from "../core/uiComponents/uiComponents.module";
import {InstanceTypeComponent} from "./instancetype.component";
import {InstanceTypeInstancesComponent} from "./instancetype.instances.component";
import {InstanceTypeService} from "./instancetype.service";
import {InstanceTypeRouting} from "./instanceType.routing";
import {ButtonModule} from "primeng/button";
import {InstanceTableModule} from "../instance-table/instance-table.module";

@NgModule({
    imports: [
        CommonModule, InstanceTypeRouting, UiComponentsModule, ButtonModule, InstanceTableModule
    ],
  declarations: [
    InstanceTypeComponent,
    InstanceTypeInstancesComponent
  ],
  providers: [
    InstanceTypeService
  ]
})

export class InstanceTypeModule {

}

