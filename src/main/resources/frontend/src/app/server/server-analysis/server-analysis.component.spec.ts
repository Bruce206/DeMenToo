import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServerAnalysisComponent } from './server-analysis.component';

describe('ServerAnalysisComponent', () => {
  let component: ServerAnalysisComponent;
  let fixture: ComponentFixture<ServerAnalysisComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServerAnalysisComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServerAnalysisComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
