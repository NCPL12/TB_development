import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'floor-energy-management',
  standalone: true,
  imports: [FormsModule, CommonModule, HttpClientModule],
  templateUrl: './floor-energy-management.component.html',
  styleUrls: ['./floor-energy-management.component.css']
})
export class FloorEnergyManagementComponent implements OnInit {
  floorEnergyMeters: any[] = [];
  currentFloorMeter: any = {};
  showForm = false;
  isEdit = false;
  showErrors = false;
  availableFloors: any[] = [];
  availableEnergyMeterNames: string[] = [];
  private apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.fetchFloorEnergyMeters();
    this.fetchAvailableFloors();
  }

  fetchAvailableEnergyMeterNames(): void {
    this.http.get<string[]>(`${this.apiBaseUrl}/available-energy-meter-names`).subscribe({
      next: (names) => {
        const assignedMeters = this.floorEnergyMeters.map((meter) => meter.name);
        this.availableEnergyMeterNames = names.filter((name) => !assignedMeters.includes(name));
      },
      error: (err) => console.error('Error fetching available energy meter names:', err)
    });
  }

  fetchAvailableFloors(): void {
    this.http.get<any[]>(`${this.apiBaseUrl}/get-all-floors`).subscribe({
      next: (floors) => {
        this.availableFloors = floors.filter(f => f.id !== 0); // Optional filtering logic
      },
      error: (err) => console.error('Error fetching floors:', err)
    });
  }

  fetchFloorEnergyMeters(): void {
    this.http.get<any[]>(`${this.apiBaseUrl}/floor-energy-meter/get-all`).subscribe({
      next: (data) => {
        this.floorEnergyMeters = data;

        // No need to fetch additional details like tenant name, unless you have a floor name service
        this.fetchAvailableEnergyMeterNames();
      },
      error: (err) => console.error('Error fetching floor energy meters:', err)
    });
  }

  onAddFloorEnergyMeter(): void {
    this.fetchAvailableEnergyMeterNames();
    this.fetchAvailableFloors();

    this.currentFloorMeter = {};
    this.isEdit = false;
    this.showForm = true;
    this.showErrors = false;
  }

  onEditFloorEnergyMeter(id: number): void {
    const meter = this.floorEnergyMeters.find((m) => m.id === id);
    if (meter) {
      this.currentFloorMeter = { ...meter };
      this.isEdit = true;
      this.showForm = true;
      this.showErrors = false;
      this.fetchAvailableFloors();
    }
  }

  validateForm(): boolean {
    return !!this.currentFloorMeter.name && !!this.currentFloorMeter.floorId;
  }

  onSaveFloorEnergyMeter(): void {
    this.showErrors = true;

    if (!this.validateForm()) {
      console.error('Form validation failed', this.currentFloorMeter);
      return;
    }

    const request = this.isEdit
      ? this.http.put(`${this.apiBaseUrl}/floor-energy-meter/update`, this.currentFloorMeter, { responseType: 'text' })
      : this.http.post(`${this.apiBaseUrl}/floor-energy-meter/add`, this.currentFloorMeter, { responseType: 'text' });

    request.subscribe({
      next: () => {
        this.showForm = false;
        this.fetchFloorEnergyMeters();
      },
      error: (err) => console.error(`Error ${this.isEdit ? 'updating' : 'adding'} floor energy meter:`, err)
    });
  }

  onCancel(): void {
    this.showForm = false;
  }
}
