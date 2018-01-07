import {Directive, ElementRef, Input, OnInit} from '@angular/core';


@Directive({
  selector: '[instance-type]'
})
export class InstanceTypeLogoDirective implements OnInit {
  @Input("instance-type") type: string;

  el: any;

  constructor(el: ElementRef) {
    this.el = el;
  }

  ngOnInit() {
    if (this.type === "XibisOne" || this.type === "SkinGo") {
      this.el.nativeElement.innerHTML = "<img src='/images/instancetypes/" + this.type + ".png' style='height: 15px;'>";
    }
  }
}
