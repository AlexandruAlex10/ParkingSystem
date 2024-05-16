import { Component, OnInit } from '@angular/core';
import { FormControl } from "@angular/forms";
import { map } from "rxjs";
import { PlateNumberService } from "../../shared/services/plateNumber.service";
import { plateNumberModel } from "../../shared/models/plateNumber.model";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})

export class HomeComponent implements OnInit {

  public plateNumbers: plateNumberModel[] = [];
  public filteredPlateNumbers: plateNumberModel[] = [];

  public currentCapacity: number = 0;
  public maxCapacity: number = 0;
  public searchText: string = '';

  public plateNumberToAddInput = new FormControl('');
  public maxCapacityInput = new FormControl('');
  public plateNumberToDeleteInput = new FormControl('');

  public openBarrierButtonState: boolean = false;
  public closeBarrierButtonState: boolean = false;

  constructor(protected plateNumberService: PlateNumberService) {
  }

  ngOnInit() {
    this.plateNumberService.getAllPlateNumbers()!.snapshotChanges().pipe(
      map(changes =>
        changes.map(c =>
          ({ key: c.payload.key, ...c.payload.val() })
        )
      )
    ).subscribe(data => {
      this.plateNumbers = data;
      this.processPlateNumbers(data);
    });

    this.plateNumberService.getCurrentCapacity().subscribe((currentCapacity) => {
      this.currentCapacity = currentCapacity;
      this.chooseColorForCapacitySpan();
    });

    this.plateNumberService.getMaxCapacity().subscribe((maxCapacity) => {
      this.maxCapacity = maxCapacity;
      this.chooseColorForCapacitySpan();
    });

    this.plateNumberService.getOpenBarrierState().subscribe((openBarrierButtonState) => {
      this.openBarrierButtonState = openBarrierButtonState;
      this.changeColorForOpenBarrierButtonOnInit();
    });

    this.plateNumberService.getCloseBarrierState().subscribe((closeBarrierButtonState) => {
      this.closeBarrierButtonState = closeBarrierButtonState;
      this.changeColorForCloseBarrierButtonOnInit();
    });

  }
  
  processPlateNumbers(data: any[]) {
    const today = new Date().toLocaleDateString('en-GB');
    this.filteredPlateNumbers = data.filter(plate => {
        if (!plate.isPermanent && !plate.reservedDates.includes(today)) return false;
        return true;
    }).map(plate => {
      return plate;
    });
  }

  public changeColorForOpenBarrierButtonOnInit() {
    const openBarrierButton = document.getElementById('open-barrier-button') as HTMLButtonElement;
    if (this.openBarrierButtonState == false) {
      openBarrierButton.style.backgroundColor = "#CC4432";
    }
    else {
      openBarrierButton.style.backgroundColor = "#9BCC32";
    }
  }

  public changeColorForCloseBarrierButtonOnInit() {
    const closeBarrierButton = document.getElementById('close-barrier-button') as HTMLButtonElement;
    if (this.closeBarrierButtonState == false) {
      closeBarrierButton.style.backgroundColor = "#CC4432";
    }
    else {
      closeBarrierButton.style.backgroundColor = "#9BCC32";
    }
  }

  public changeColorForOpenBarrierButton() {
    const openBarrierButton = document.getElementById('open-barrier-button') as HTMLButtonElement;
    const closeBarrierButton = document.getElementById('close-barrier-button') as HTMLButtonElement;
    if (this.openBarrierButtonState == false) {
      if (this.closeBarrierButtonState == true) {
        this.closeBarrierButtonState = false;
        closeBarrierButton.style.backgroundColor = "#CC4432";
        this.plateNumberService.updateCloseBarrierIndefinitely(this.closeBarrierButtonState);
      }
      this.openBarrierButtonState = true;
      openBarrierButton.style.backgroundColor = "#9BCC32";
      this.plateNumberService.updateOpenBarrierIndefinitely(this.openBarrierButtonState);
    }
    else {
      this.openBarrierButtonState = false;
      openBarrierButton.style.backgroundColor = "#CC4432";
      this.plateNumberService.updateOpenBarrierIndefinitely(this.openBarrierButtonState);
    }
  }

  public changeColorForCloseBarrierButton() {
    const openBarrierButton = document.getElementById('open-barrier-button') as HTMLButtonElement;
    const closeBarrierButton = document.getElementById('close-barrier-button') as HTMLButtonElement;
    if (this.closeBarrierButtonState == false) {
      if (this.openBarrierButtonState == true) {
        this.openBarrierButtonState = false;
        openBarrierButton.style.backgroundColor = "#CC4432";
        this.plateNumberService.updateOpenBarrierIndefinitely(this.openBarrierButtonState);
      }
      this.closeBarrierButtonState = true;
      closeBarrierButton.style.backgroundColor = "#9BCC32";
      this.plateNumberService.updateCloseBarrierIndefinitely(this.closeBarrierButtonState);
    }
    else {
      this.closeBarrierButtonState = false;
      closeBarrierButton.style.backgroundColor = "#CC4432";
      this.plateNumberService.updateCloseBarrierIndefinitely(this.closeBarrierButtonState);
    }
  }

  public chooseColorForCapacitySpan() {
    const capacitySpan = document.getElementById('capacity-span') as HTMLSpanElement;
    if ((this.currentCapacity == 0 && this.maxCapacity == 0) || this.currentCapacity / this.maxCapacity < 0.5) {
      capacitySpan.style.color = "#9BCC32";
    }
    else if (this.currentCapacity / this.maxCapacity >= 1) {
      capacitySpan.style.color = "#CC4432";
    }
    else {
      capacitySpan.style.color = "#FFC800";
    }
  }

  public onSubmitPlateNumber() {
    if (this.plateNumberToAddInput.value == '') {
      this.alertMessageEmpty();
      return;
    }
    else {
      if (this.plateNumberToAddInput.value != null) {
        const regexPlateNumber = /^(((AB|AG|AR|BC|BH|BN|BR|BT|BV|BZ|CJ|CL|CS|CT|CV|DB|DJ|GJ|GL|GR|HD|HR|IF|IL|IS|MH|MM|MS|NT|OT|PH|SB|SJ|SM|SV|TL|TM|TR|VL|VN|VS) (0[1-9]|[1-9][0-9]))|(B (0[1-9]|[1-9][0-9]{1,2}))) ([A-HJ-NPR-Z][A-PR-Z]{2})$/;
        const isMatch = regexPlateNumber.test(this.plateNumberToAddInput.value);
        if (!isMatch) {
          this.alertMessageInvalidPlateNumber();
          return;
        }
      }
      this.plateNumberService.postNewPlateNumber(this.plateNumberToAddInput.value, true);
      this.plateNumberToAddInput.reset('');
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
      this.plateNumberService.updateMaxCapacity(this.maxCapacityInput.value);
      this.maxCapacityInput.reset('');
    }
  }

  filterResults(text: string) {
    if (!text) {
      this.filteredPlateNumbers = this.plateNumbers;
      this.processPlateNumbers(this.filteredPlateNumbers)
      return;
    }
    this.filteredPlateNumbers = this.plateNumbers.filter(
      plateNumbers => plateNumbers?.plateNumber?.toLowerCase().includes(text.toLowerCase())
    );
    this.processPlateNumbers(this.filteredPlateNumbers)
  }

  onDeletePlateNumber() {
    if (this.plateNumberToDeleteInput.value == '') {
      this.alertMessageEmpty();
      return;
    }
    else {
      if (this.plateNumberToDeleteInput.value != null) {
        if (confirm("Are you sure you want to delete plate number " + this.plateNumberToDeleteInput.value + "?")) {
          this.plateNumberService.deletePlateNumber(this.plateNumberToDeleteInput.value);
          this.plateNumberToDeleteInput.reset('');
        }
      }
    }
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