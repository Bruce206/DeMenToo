import {APP_INITIALIZER, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {HttpClientModule, HTTP_INTERCEPTORS} from "@angular/common/http";
import {AppRoutingModule} from "./app.routing";
import {CustomHttpInterceptor} from "./security/CustomHttpInterceptor";
import {ButtonModule} from "primeng/button";
import {CurrentUserService} from "./core/current-user.service";
import {TitleService} from "./core/title.service";
import {UiComponentsModule} from "./core/uiComponents/uiComponents.module";
import {MenubarModule} from 'primeng/menubar';
import {PendingChangesGuard} from "./core/ComponentCanDeactivate";
import {ToastModule} from "primeng/toast";
import {ConfirmationService, MessageService} from "primeng/api";
import {TooltipModule} from "primeng/tooltip";
import {ConfirmDialogModule} from "primeng/confirmdialog";
import {TieredMenuModule} from "primeng/tieredmenu";
import {SortPipe} from "./sort.pipe";
import {DashboardComponent} from "./dashboard/dashboard.component";

const appInitializerFn = (currentUserService: CurrentUserService) => {
	return () => {
		console.log("Initializing backend...");
		return Promise.all([currentUserService.init()]);
	}
};

@NgModule({
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    FormsModule, ButtonModule,
    ReactiveFormsModule,
    HttpClientModule,
    MenubarModule,
    UiComponentsModule, ToastModule, TooltipModule, ConfirmDialogModule, TieredMenuModule,
  ],
	declarations: [
		AppComponent,
    DashboardComponent
	],
	providers: [
		CurrentUserService,
		MessageService,
		TitleService,
    PendingChangesGuard,
    ConfirmationService,
    SortPipe,
    {
			provide: HTTP_INTERCEPTORS,
			useClass: CustomHttpInterceptor,
			multi: true
		},
		{
			provide: APP_INITIALIZER,
			useFactory: appInitializerFn,
			multi: true,
			deps: [CurrentUserService]
		}
	],
	bootstrap: [AppComponent]
})
export class AppModule {

}
