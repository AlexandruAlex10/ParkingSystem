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

  public currentCapacity: number = 0;
  public maxCapacity: number = 0;
  public searchText: string = '';

  public plateNumberToAddInput = new FormControl('');
  public maxCapacityInput = new FormControl('');
  public plateNumberToDeleteInput = new FormControl('');

  public openBarrierButtonState: boolean = false;
  public closeBarrierButtonState: boolean = false;

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

    this.inmatriculareService.getCurrentCapacity().subscribe((currentCapacity) => {
      this.currentCapacity = currentCapacity;
      this.chooseColorForCapacitySpan();
    });

    this.inmatriculareService.getMaxCapacity().subscribe((maxCapacity) => {
      this.maxCapacity = maxCapacity;
      this.chooseColorForCapacitySpan();
    });

    this.inmatriculareService.getOpenBarrierState().subscribe((openBarrierButtonState) => {
      this.openBarrierButtonState = openBarrierButtonState;
      this.changeColorForOpenBarrierButtonOnInit();
    });

    this.inmatriculareService.getCloseBarrierState().subscribe((closeBarrierButtonState) => {
      this.closeBarrierButtonState = closeBarrierButtonState;
      this.changeColorForCloseBarrierButtonOnInit();
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
        this.inmatriculareService.updateCloseBarrierIndefinitely(this.closeBarrierButtonState);
      }
      this.openBarrierButtonState = true;
      openBarrierButton.style.backgroundColor = "#9BCC32";
      this.inmatriculareService.updateOpenBarrierIndefinitely(this.openBarrierButtonState);
    }
    else {
      this.openBarrierButtonState = false;
      openBarrierButton.style.backgroundColor = "#CC4432";
      this.inmatriculareService.updateOpenBarrierIndefinitely(this.openBarrierButtonState);
    }
  }

  public changeColorForCloseBarrierButton() {
    const openBarrierButton = document.getElementById('open-barrier-button') as HTMLButtonElement;
    const closeBarrierButton = document.getElementById('close-barrier-button') as HTMLButtonElement;
    if (this.closeBarrierButtonState == false) {
      if (this.openBarrierButtonState == true) {
        this.openBarrierButtonState = false;
        openBarrierButton.style.backgroundColor = "#CC4432";
        this.inmatriculareService.updateOpenBarrierIndefinitely(this.openBarrierButtonState);
      }
      this.closeBarrierButtonState = true;
      closeBarrierButton.style.backgroundColor = "#9BCC32";
      this.inmatriculareService.updateCloseBarrierIndefinitely(this.closeBarrierButtonState);
    }
    else {
      this.closeBarrierButtonState = false;
      closeBarrierButton.style.backgroundColor = "#CC4432";
      this.inmatriculareService.updateCloseBarrierIndefinitely(this.closeBarrierButtonState);
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
      this.inmatriculareService.postNewPlateNumber(this.plateNumberToAddInput.value);
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

  onDeletePlateNumber() {
    if (this.plateNumberToDeleteInput.value == '') {
      this.alertMessageEmpty();
      return;
    }
    else {
      if (this.plateNumberToDeleteInput.value != null) {
        if (confirm("Are you sure you want to delete plate number " + this.plateNumberToDeleteInput.value + "?")) {
          this.inmatriculareService.deletePlateNumber(this.plateNumberToDeleteInput.value);
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