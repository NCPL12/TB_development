import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { LoginService } from '../../services/login.service';
import { environment } from '../../../environments/environment';



@Component({
  selector: 'app-automated-bill',
  standalone: true,
    imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './automated-bill.component.html',
  styleUrl: './automated-bill.component.css'
})
export class AutomatedBillComponent {
  billingHistory: any[] = [];
  filteredBillingHistory: any[] = [];
  tenants: any[] = [];
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
    this.fetchBillingHistory();
    this.loadTenants();
  }

  fetchBillingHistory(): void {
    this.http.get<any[]>(`${environment.apiBaseUrl}/scheduled-bills/all-auto`).subscribe({
      next: (data) => {
        // Store the full fetched data in a component variable
        this.billingHistory = data;
        
        // Update the UI or perform additional logic
        this.updateDisplayedTenants();
        this.filterRecords();
        this.sortBillingHistory();
      },
      error: (error) => {
        // Handle the error appropriately
        this.errorMessage = "Failed to load billing history. Please try again later.";
        console.error("Error fetching billing history:", error);
      }
    });
  }
  
  loadTenants(): void {
    this.http.get<any[]>(`${environment.apiBaseUrl}/get-all-tenants`).subscribe({
      next: (data) => {
        this.tenants = data;
        this.updateDisplayedTenants();
      },
      error: () => console.error('Error fetching tenants')
    });
  }


  
  updateDisplayedTenants(): void {
    if (this.billingHistory.length > 0 && this.tenants.length > 0) {
      this.billingHistory.forEach((record) => {
        const tenant = this.tenants.find((t) => t.id === record.tenant_id);
        record.tenantName = tenant ? tenant.name : 'Unknown Tenant';
      });
    }
  }
  filterRecords(): void {
    const searchText = this.searchText.toLowerCase();
    this.filteredBillingHistory = this.billingHistory.filter(
      (record) =>
        record.bill_name?.toLowerCase().includes(searchText) || 
        record.tenantName?.toLowerCase().includes(searchText)
    );
  }



  highlightSearch(record: any): boolean {
    return this.searchText && record.bill_name?.toLowerCase().includes(this.searchText.toLowerCase());
  }

  sortBillingHistory(): void {
    this.filteredBillingHistory.sort((a, b) => {
      return new Date(b.generated_date).getTime() - new Date(a.generated_date).getTime();
    });
  }

 toggleSortOrder(): void {
    this.isAscending = !this.isAscending;
    this.filteredBillingHistory.sort((a, b) => {
      return this.isAscending ? 
        new Date(a.generated_date).getTime() - new Date(b.generated_date).getTime() : 
        new Date(b.generated_date).getTime() - new Date(a.generated_date).getTime();
    });
  }


  openPdfPopup(billId: number): void {
    const url = `${environment.apiBaseUrl}/scheduled-bills/${billId}/pdf-view`;
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
