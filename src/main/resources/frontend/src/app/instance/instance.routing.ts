import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {InstanceListComponent} from "./instance.list.component";
import {InstanceDetailComponent} from "./instance.detail.component";

const customerRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: InstanceListComponent,
    data: {title: 'Instanzen'}
  },
  {
    path: '/detail/:id',
    component: InstanceDetailComponent,
    data: {title: 'Instanz'}
  }
];

@NgModule({
  imports: [RouterModule.forChild(customerRoutes)],
  exports: [RouterModule]
})
export class InstanceRouting { }
