import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManualReportComponent } from './manual-report.component';

describe('ManualReportComponent', () => {
  let component: ManualReportComponent;
  let fixture: ComponentFixture<ManualReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManualReportComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ManualReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
