import { Injectable } from '@angular/core';
import {
	HttpRequest,
	HttpHandler,
	HttpEvent,
	HttpInterceptor, HttpResponse, HttpErrorResponse
} from '@angular/common/http';
import { Observable } from 'rxjs';
import {tap, timeout} from "rxjs/operators";
@Injectable()
export class CustomHttpInterceptor implements HttpInterceptor {
	constructor() {}
	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		return next.handle(request).pipe(timeout(100000000), tap((event: HttpEvent<any>) => {
			if (event instanceof HttpResponse) {
				// console.log(event);
				// do stuff with response if you want
			}
		}, (err: any) => {
			if (err instanceof HttpErrorResponse) {
				if (err.status === 401) {
					window.location.href = "/login";
				}
			}
		}));
	}
}
