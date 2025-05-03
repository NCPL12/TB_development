import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-manual-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manual-report.component.html',
  styleUrls: ['./manual-report.component.css']
})
export class ManualReportComponent {
  fromDate: string = '';
  toDate: string = '';
  showErrors: boolean = false;
  private apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {
    this.setDefaultDates();
  }

  setDefaultDates(): void {
    const today = new Date();
    const lastMonthStart = new Date(today.getFullYear(), today.getMonth() - 1, 1);
    const lastMonthEnd = new Date(today.getFullYear(), today.getMonth(), 0);

    this.fromDate = lastMonthStart.toISOString().split('T')[0];
    this.toDate = lastMonthEnd.toISOString().split('T')[0];
  }

  onInputChange(): void {
    this.showErrors = !(this.fromDate && this.toDate);
  }

  exportPdfReport(): void {
    if (!this.fromDate || !this.toDate) {
      this.showErrors = true;
      return;
    }

    const endpoint = `${this.apiBaseUrl}/manual-floor-usage/export`;
    const queryParams = `?fromDate=${this.fromDate}&toDate=${this.toDate}`;
    window.open(endpoint + queryParams, '_blank');
  }
}
