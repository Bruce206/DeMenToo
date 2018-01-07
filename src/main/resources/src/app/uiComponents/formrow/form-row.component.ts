import {
  Component, ContentChild, EventEmitter, forwardRef, Input, OnInit, Output, TemplateRef,
  ViewChild
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

const INLINE_EDIT_CONTROL_VALUE_ACCESSOR = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => FormRowComponent),
  multi: true
};

@Component({
  selector: 'form-row',
  templateUrl:'form-row.component.html',
  providers: [INLINE_EDIT_CONTROL_VALUE_ACCESSOR],
  styleUrls: ['form-row.component.css']
})


export class FormRowComponent implements ControlValueAccessor, OnInit {

  @ViewChild('inlineEdit') inlineEdit; // input DOM element
  @Input() label: string = '';  // Label value for input element
  @Input() type: string = 'text'; // The type of input element (not required if options are present)
  @Input() required: boolean = false; // Is input requried?
  @Input() disabled: boolean = false; // Is input disabled?
  @Input() editable: boolean = false;  // Is input editable?
  @Input() permanentFormField: boolean = false;  // Is input editable?
  @Input() options: Array<any>;  // Options (optional)
  @Input() optionLabel: string; // attributename of the label to show for an option - leave empty to show the whole item as label (usable for string-Arrays)
  @Input() optionValue: string; //attributename of the value to set for an option - leave empty for setting the whole item as value
  @Input() rows: number; // if set, a textarea is shown instead of an input box
  @Output("submitted") submitted: EventEmitter<any> = new EventEmitter();  // Label value for input element

  @ContentChild(TemplateRef) innerTemplate;

  private _value: string = ''; // Private variable for input value
  private preValue: string = ''; // The value before clicking to edit
  private editing: boolean = false; // Is Component in edit mode?
  public onChange: any = Function.prototype; // Trascend the onChange event
  public onTouched: any = Function.prototype; // Trascend the onTouch event

  constructor() {

  }

  ngOnInit() {
  }

  // Do stuff when the input element loses focus
  save($event: Event) {
    this.editing = false;
    if (this.preValue !== this.value) {
      this.submitted.emit($event);
    }
  }

  // Start the editting process for the input element
  edit(value) {
    if (this.disabled || !this.editable) {
      return;
    }

    this.preValue = value;
    this.editing = true;
    // Focus on the input element just as the editing begins
    setTimeout((_: any) => this.inlineEdit.nativeElement.focus(), 'focus', []);
  }

  getValue(value:any): string {
    if (this.options) {
      for (let o of this.options) {
        let v = this.optionValue ? o[this.optionValue] : o;
        if (v === value) {
          return this.optionLabel ? o[this.optionLabel] : o;
        }
      }
    }

    return value;
  }

  // Control Value Accessors for ngModel
  get value(): any {
    return this._value;
  }

  set value(v: any) {
    if (v !== this._value) {
      this._value = v;
      this.onChange(v);
    }
  }

  // Required for ControlValueAccessor interface
  writeValue(value: any) {
    this._value = value;
  }

  // Required forControlValueAccessor interface
  public registerOnChange(fn: (_: any) => {}): void {
    this.onChange = fn;
  }

  // Required forControlValueAccessor interface
  public registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

}
