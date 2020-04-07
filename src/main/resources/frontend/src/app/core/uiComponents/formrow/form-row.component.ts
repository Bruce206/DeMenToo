import {
  Component, ContentChild, ElementRef, EventEmitter, forwardRef, Input, OnInit, Output, TemplateRef,
  ViewChild
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import * as moment from 'moment';

const INLINE_EDIT_CONTROL_VALUE_ACCESSOR = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => FormRowComponent),
  multi: true
};

@Component({
  selector: 'form-row',
  templateUrl: 'form-row.component.html',
  styleUrls: ['form-row.component.css'],
  providers: [INLINE_EDIT_CONTROL_VALUE_ACCESSOR]
})


export class FormRowComponent implements ControlValueAccessor, OnInit {

  @ViewChild('inlineEdit') inlineEdit; // input DOM element
  @Input() label: string = '';  // Label value for input element
  @Input() type: string = 'text'; // The type of input element (not required if options are present)
  @Input() required: boolean = false; // Is input requried?
  @Input() disabled: boolean = false; // Is input disabled?
  @Input() editable: boolean = false;  // Is input editable?
  @Input() permanent: boolean = true;  // Don't show click to edit. Always show input
  @Input() options: Array<any>;  // Options (optional)
  @Input() optionsType: string = 'select'; // [select, listbox] -> LISTBOX ONLY WORKS WITH LABEL / VALUE PAIRS ATM!!!
  @Input() optionLabel: string = 'label'; // attributename of the label to show for an option - leave empty for default: 'label'
  @Input() optionValue: string = 'value'; // attributename of the value to set for an option - leave empty for default: 'value' - set "WHOLE_OBJECT" for selecting the whole object.. duh...
  @Input() rows: number; // if set, a textarea is shown instead of an input box
  @Input() multiple: boolean; // if set, a selection of multiple options is allowed (options required!)
  @Input() multipleType: string; // checkbox or mutliselect
  @Input() trackBy: string = ''; // track options by this attribute
  @Input() clearable: boolean = false;  // Adds a reset-switch to clear the input
  @Input() multiLine: boolean = false; // split label and input to seperate lines
  @Input() labelWidth: number = 5;
  @Input() styleClass: string = ''; // class to attach to form-row div
  @Input() autofocus: boolean = false;
  @Input() tabindex: number;
  @Input() appendTo: any;
  @Output('submitted') submitted: EventEmitter<any> = new EventEmitter();  // Label value for input element

  @Output('onFocus') onFocus: EventEmitter<ElementRef> = new EventEmitter<ElementRef>();
  @Output() blur: EventEmitter<any> = new EventEmitter<any>();

  @ContentChild(TemplateRef) innerTemplate;

  private _value: string = ''; // Private variable for input value
  private preValue: string = ''; // The value before clicking to edit
  public editing: boolean = false; // Is Component in edit mode?
  public onChange: any = Function.prototype; // Trascend the onChange event
  public onTouched: any = Function.prototype; // Trascend the onTouch event

  constructor() {

  }

  ngOnInit() {
  }

  ngAfterViewInit() {
  }

  // Do stuff when the input element loses focus
  save($event: Event) {
    this.editing = false;
    if (this.preValue !== this.value) {
      this.submitted.emit($event);
    }
    this.blur.emit();
  }

  // Do stuff when the input element loses focus
  clear($event: Event) {
    this._value = undefined;
    this.onChange(undefined);
  }

  // Start the editting process for the input element
  edit(value) {
    if (this.disabled || !this.editable) {
      return;
    }

    this.preValue = value;
    this.editing = true;
    // Focus on the input element just as the editing begins
    // setTimeout((_: any) => this.inlineEdit.nativeElement.focus());
  }

  getValue(value: any): string {
    if (this.options) {
      if (!this.multiple) {
        for (const o of this.options) {
          if (this.optionValue === 'WHOLE_OBJECT') {
            return o;
          } else {
            const v = this.optionValue ? o[this.optionValue] : o.value;
            if (v === value) {
              return this.optionLabel ? o[this.optionLabel] : o.label;
            }
          }
        }
      } else {
        const returnValues = [];
        if (value != null && Array.isArray(value)) {
          for (const sv of value) {
            for (const o of this.options) {
              if (this.optionValue === 'WHOLE_OBJECT') {
                returnValues.push(o);
              } else {
                const v = this.optionValue ? o[this.optionValue] : o.value;
                if (v === sv) {
                  returnValues.push(this.optionLabel ? o[this.optionLabel] : o.label);
                }
              }
            }
          }
          return returnValues.join(', ');
        } else {
          // console.log("Value is null || (not an array but multiple): " + this.label);
        }
      }
    }

    return Array.isArray(value) ? value.join(', ') : value;
  }

  // Control Value Accessors for ngModel
  get value(): any {
    return this._value;
  }

  set value(v: any) {
    if (v !== this._value) {
      this._value = v;

      if (this.type === "date") {
        // convert js-date from p-calendar back to string for saving
        v.setHours(12);
        this.onChange(moment(v).format("YYYY-MM-DD"));
      } else {
        this.onChange(v);
      }
    }
  }

  // Required for ControlValueAccessor interface
  writeValue(value: any) {
    if (this.inlineEdit && this.inlineEdit.nativeElement && this.inlineEdit.nativeElement.nodeName === 'TEXTAREA') {
      this.inlineEdit.nativeElement.style.height = (this.rows * 22) + "px";
    }
    if (!this.multiple && value instanceof Array) {
      if (value.length > 0) {
        value = value[0];
      } else {
        value = undefined;
      }
    }

    if (this.type === "date" && value) {
      // convert input string from ngModel to javascript-date to allow p-calendar to work with it
      value = moment(value).toDate();
    }

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

  compareOptions(o1, o2): boolean {
    if (this.trackBy) {
      if (o1 === undefined || o2 === undefined || o1 === null || o2 === null) {
        return false;
      }
      return o1[this.trackBy] === o2[this.trackBy];
    } else {
      return o1 === o2;
    }
  }

  getLabelClass() {
    if (this.multiLine) {
      return 'p-col-12';
    } else {
      return 'p-col-' + this.labelWidth;
    }
  }

  getValueClass() {
    if (this.multiLine) {
      return 'p-col-12';
    } else {
      return 'p-col-' + (12 - this.labelWidth);
    }
  }

  focus() {
    this.inlineEdit.nativeElement.focus();
  }

  focused() {
    this.onFocus.emit(this.inlineEdit);
  }
}
