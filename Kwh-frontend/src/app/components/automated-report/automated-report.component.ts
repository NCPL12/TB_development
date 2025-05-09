import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-automated-report',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './automated-report.component.html',
  styleUrls: ['./automated-report.component.css']
})
export class AutomatedReportComponent implements OnInit {

  reportHistory: any[] = [];
  filteredReportHistory: any[] = [];
  isAscending = false;
  searchText = '';
  errorMessage = '';
  showPdfPopup = false;
  sanitizedPdfUrl: SafeResourceUrl | null = null;
  pdfUrl: SafeResourceUrl | null = null;

  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.fetchReportHistory();
  }

  fetchReportHistory(): void {
    this.http.get<any[]>(`${environment.apiBaseUrl}/scheduled-floor-reports/report-all`).subscribe({
      next: (data) => {
        this.reportHistory = data;
        this.filterRecords(); // also sorts
      },
      error: () => {
        this.errorMessage = "Failed to load automated reports. Please try again later.";
      }
    });
  }

  filterRecords(): void {
    const searchText = this.searchText.toLowerCase();
    this.filteredReportHistory = this.reportHistory.filter(
      (record) =>
        record.report_name?.toLowerCase().includes(searchText) ||
        record.periods?.toLowerCase().includes(searchText)
    );
    this.sortFilteredRecords();
  }

  highlightSearch(record: any): boolean {
    return this.searchText && record.report_name?.toLowerCase().includes(this.searchText.toLowerCase());
  }

  toggleSortOrder(): void {
    this.isAscending = !this.isAscending;
    this.sortFilteredRecords();
  }

  sortFilteredRecords(): void {
    this.filteredReportHistory.sort((a, b) => {
      const dateA = new Date(a.generated_date || a.generatedDate).getTime();
      const dateB = new Date(b.generated_date || b.generatedDate).getTime();
      return this.isAscending ? dateA - dateB : dateB - dateA;
    });
  }

  openPdfPopup(reportId: number): void {
    const url = `${environment.apiBaseUrl}/scheduled-floor-reports/${reportId}/pdf-view`;
    this.sanitizedPdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);  
    this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
    this.showPdfPopup = true;

  }

  closePdfPopup(): void {
    this.showPdfPopup = false;
    if (this.pdfUrl) {
      URL.revokeObjectURL(this.sanitizedPdfUrl as any);
      this.sanitizedPdfUrl = null;
    }
  }
}
