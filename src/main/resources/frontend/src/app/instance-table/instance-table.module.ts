import {NgModule} from '@angular/core';
import {CommonModule} from "@angular/common";
import {UiComponentsModule} from "../core/uiComponents/uiComponents.module";
import {ButtonModule} from "primeng/button";
import {InstanceTableComponent} from "./instance-table.component";
import {TableModule} from "primeng/table";
import {MultiSelectModule} from "primeng/multiselect";

@NgModule({
  imports: [
    CommonModule, UiComponentsModule, ButtonModule, TableModule, MultiSelectModule
  ],
  exports: [
    InstanceTableComponent
  ],
  declarations: [
    InstanceTableComponent
  ]
})

export class InstanceTableModule {

}

