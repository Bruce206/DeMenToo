import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {InstanceListComponent} from "./instance.list.component";
import {InstanceDetailComponent} from "./instance.detail.component";

const routes: Routes = [
  {
    path: '',
    component: InstanceListComponent,
    data: {title: 'Instanzen'}
  },
  {
    path: 'detail/:id',
    pathMatch: 'full',
    component: InstanceDetailComponent,
    data: {title: 'Instanz'}
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InstanceRouting { }
