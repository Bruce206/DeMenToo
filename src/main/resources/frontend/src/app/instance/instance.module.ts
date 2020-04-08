import {NgModule} from '@angular/core';
import {InstanceDetailComponent} from "./instance.detail.component";
import {InstanceListComponent} from "./instance.list.component";
import {CommonModule} from "@angular/common";
import {UiComponentsModule} from "../core/uiComponents/uiComponents.module";
import {InstanceRouting} from "./instance.routing";
import {MultiSelectModule} from "primeng/multiselect";
import {TableModule} from "primeng/table";
import {ButtonModule} from "primeng/button";
import {InstanceTableModule} from "../instance-table/instanceTable.module";

@NgModule({
    imports: [
        CommonModule, InstanceRouting, UiComponentsModule, MultiSelectModule, TableModule, ButtonModule, InstanceTableModule
    ],
  declarations: [
    InstanceDetailComponent,
    InstanceListComponent
  ],
  providers: [
  ]
})

export class InstanceModule {

}

