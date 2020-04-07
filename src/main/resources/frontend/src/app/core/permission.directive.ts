import {Directive, ElementRef, Input, OnInit, Renderer2} from '@angular/core';
import {CurrentUserService, HasPermission} from './current-user.service';

@Directive({
  selector: '[hasPermission]'
})
export class HasPermissionDirective implements OnInit {

  @Input() hasPermission: HasPermission[];

  constructor(private el: ElementRef, private renderer: Renderer2, private currentUserService: CurrentUserService) {

  }

  ngOnInit(): void {
    console.log(this.hasPermission);

    let permissionResult: boolean = false;
    for (const p of this.hasPermission) {
      if (this.currentUserService.hasPermission(p)) {
        permissionResult = true;
        break;
      }
    }

    if (permissionResult === false) {
      this.renderer.setStyle(this.el.nativeElement, 'display', 'none');
    }
  }
}
