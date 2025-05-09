import { Component, OnInit } from "@angular/core";
import { HttpClient, HttpClientModule } from "@angular/common/http";
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { environment } from "../../../environments/environment";
import { LoginService } from "../../services/login.service";

@Component({
  selector: "app-report-history",
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: "./report-history.component.html",
  styleUrls: ["./report-history.component.css"],
})
export class ReportHistoryComponent implements OnInit {

  reportHistory: any[] = [];
  filteredReportHistory: any[] = [];
  isAscending = false;
  searchText = "";
  errorMessage = "";
  showPdfPopup = false;
  sanitizedPdfUrl: SafeResourceUrl | null = null;
  role: string = "";
  userName: string = "";
  pdfUrl: SafeResourceUrl | null = null;


  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private loginService: LoginService
  ) {}

  ngOnInit(): void {
    this.loadUserData();
  }

  loadUserData(): void {
    this.userName = this.loginService.getUsername() || "Guest";
    this.role = this.loginService.getRole() || "";
    this.fetchReportHistory();
  }

  fetchReportHistory(): void {
    this.http.get<any[]>(`${environment.apiBaseUrl}/all-report-histories`).subscribe({
      next: (data) => {
        this.reportHistory = data;
        this.filterRecords(); // also sorts
      },
      error: () => this.errorMessage = "Failed to load report history. Please try again later."
    });
  }

  filterRecords(): void {
    const searchText = this.searchText.toLowerCase();
    this.filteredReportHistory = this.reportHistory.filter(
      (record) =>
        record.report_name?.toLowerCase().includes(searchText) ||
        record.type?.toLowerCase().includes(searchText)
    );
    this.sortFilteredRecords(); // Apply sort after filtering
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
      const dateA = new Date(a.generated_date).getTime();
      const dateB = new Date(b.generated_date).getTime();
      return this.isAscending ? dateA - dateB : dateB - dateA;
    });
  }

  formatDate(dateString: string): string {
    // Check if it's a date range (contains 'to')
    if (dateString.includes('to')) {
      const [start, end] = dateString.split(' to ').map(date => {
        const parts = date.split('-');
        return `${parts[2]}-${parts[1]}-${parts[0]}`;
      });
      return `${start} to ${end}`;
    }

    // Try different date formats
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {
      // Try another format
      const parts = dateString.split('-');
      if (parts.length === 3) {
        return `${parts[0]}-${parts[1]}-${parts[2]}`;
      }
      return dateString;
    }
    return date.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).replace(/\//g, '-');
  }

  openPdfPopup(reportId: number): void {
    const url = `${environment.apiBaseUrl}/report-history-pdf/${reportId}`;
    this.sanitizedPdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);  
    this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
    this.showPdfPopup = true;
  }

  closePdfPopup(): void {
    this.showPdfPopup = false;
    if (this.sanitizedPdfUrl) {
      URL.revokeObjectURL(this.sanitizedPdfUrl as any);
      this.sanitizedPdfUrl = null;
    }
  }
}
