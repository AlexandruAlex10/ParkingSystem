import { Component, OnInit } from '@angular/core';
import { FormControl } from "@angular/forms";
import { map } from "rxjs";
import { InmatriculareService } from "../../shared/services/plateNumber.service";
import { plateNumberModel } from "../../shared/models/plateNumber.model";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})

export class HomeComponent implements OnInit {

  public plateNumbers: plateNumberModel[] = [];
  public filteredPlateNumbers: plateNumberModel[] = [];
  
  public maxCapacity: number = 0;
  public searchText: string = '';

  public plateNumberInput = new FormControl('');
  public maxCapacityInput = new FormControl('');

  constructor(protected inmatriculareService: InmatriculareService) {
  }

  ngOnInit() {
    this.inmatriculareService.getAllPlateNumbers()!.snapshotChanges().pipe(
      map(changes =>
        changes.map(c =>
          ({ key: c.payload.key, ...c.payload.val() })
        )
      )
    ).subscribe(data => {
      this.plateNumbers = data;
      this.filteredPlateNumbers = data;
    });

    this.inmatriculareService.getMaxCapacity().subscribe((maxCapacity) => {
      this.maxCapacity = maxCapacity;
    });
  }

  public onSubmitPlateNumber() {
    if (this.plateNumberInput.value == '') {
      this.alertMessageEmpty();
      return;
    }
    else {
      if (this.plateNumberInput.value != null) {
        const regexPlateNumber = /^(((AB|AG|AR|BC|BH|BN|BR|BT|BV|BZ|CJ|CL|CS|CT|CV|DB|DJ|GJ|GL|GR|HD|HR|IF|IL|IS|MH|MM|MS|NT|OT|PH|SB|SJ|SM|SV|TL|TM|TR|VL|VN|VS) (0[1-9]|[1-9][0-9]))|(B (0[1-9]|[1-9][0-9]{1,2}))) ([A-HJ-NPR-Z][A-PR-Z]{2})$/;
        const isMatch = regexPlateNumber.test(this.plateNumberInput.value);
        if (!isMatch) {
          this.alertMessageInvalidPlateNumber();
          return;
        }
      }
      this.inmatriculareService.post(this.plateNumberInput.value);
      this.plateNumberInput.reset('');
    }
  }

  public onSubmitMaxCapacity() {
    if (this.maxCapacityInput.value == '') {
      this.alertMessageEmpty();
      return;
    }
    else {
      if (this.maxCapacityInput.value != null) {
        const regexMaxCapacity = /^(([1-9])|([1-9][0-9]{1,2}))$/;
        const isMatch = regexMaxCapacity.test(this.maxCapacityInput.value);
        if (!isMatch) {
          this.alertMessageInvalidMaxCapacity();
          return;
        }
      }
      this.inmatriculareService.updateMaxCapacity(this.maxCapacityInput.value);
      this.maxCapacityInput.reset('');
    }
  }

  filterResults(text: string) {
    if (!text) {
      this.filteredPlateNumbers = this.plateNumbers;
      return;
    }
    this.filteredPlateNumbers = this.plateNumbers.filter(
      plateNumbers => plateNumbers?.nrInmatriculare?.toLowerCase().includes(text.toLowerCase())
    );
  }

  deletePlateNumber(nr: any) {
    this.inmatriculareService.deleteNumarInmatriculare(nr);
  }

  public alertMessageEmpty() {
    alert("Operation aborted: field was empty!");
  }

  public alertMessageInvalidPlateNumber() {
    alert("Operation aborted: field was invalid!\n\nRules:\nhttps://en.wikipedia.org/wiki/Vehicle_registration_plates_of_Romania");
  }

  public alertMessageInvalidMaxCapacity() {
    alert("Operation aborted: field was invalid!\n\nPlease enter a number between 1 and 999");
  }
}