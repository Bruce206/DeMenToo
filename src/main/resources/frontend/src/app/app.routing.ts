import {Routes, RouterModule} from '@angular/router';
import {NgModule} from "@angular/core";
import {DashboardComponent} from "./dashboard/dashboard.component";

const appRoutes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    component: DashboardComponent
  },
  {
    path: 'server',
    loadChildren: () => import('./server/server.module').then(m => m.ServerModule)
  },
  {
    path: 'instance',
    loadChildren: './instance/instance.module#InstanceModule'
    //loadChildren: () => import('./instance/instance.module').then(m => m.InstanceModule)
  },
  // {
  //   path: 'instanceType',
  //   loadChildren: () => import('./instancetype/instanceType.module').then(m => m.InstanceTypeModule)
  // }
];

@NgModule({
  imports: [RouterModule.forRoot(appRoutes)],
  exports: [RouterModule],
  providers: []
})
export class AppRoutingModule {
}
