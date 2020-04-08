import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormRowComponent} from './formrow/form-row.component';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {CheckboxModule} from 'primeng/checkbox';
import {DialogModule} from 'primeng/dialog';
import {ButtonModule} from 'primeng/button';
import {TabViewModule} from 'primeng/tabview';
import {ListboxModule} from 'primeng/listbox';
import {HasPermissionDirective} from "../permission.directive";
import {InputTextModule} from "primeng/inputtext";
import {CalendarModule} from "primeng/calendar";
import {InputTextareaModule} from "primeng/inputtextarea";
import {MultiSelectModule} from "primeng/multiselect";
import {SortPipe} from "../../sort.pipe";
import {SafeUrlPipe} from "../safe-url.pipe";

@NgModule({
  imports: [
    CommonModule, FormsModule, MultiSelectModule, CheckboxModule, DialogModule, ButtonModule,
    TabViewModule, RouterModule, ListboxModule, InputTextModule, InputTextareaModule, CalendarModule
  ],
  declarations: [
    FormRowComponent,
    HasPermissionDirective,
    SortPipe,
    SafeUrlPipe
  ],
  exports: [
    FormRowComponent,
    FormsModule,
    HasPermissionDirective,
    SortPipe,
    SafeUrlPipe
  ],
})
export class UiComponentsModule {
}
