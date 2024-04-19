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
  public maxCapacity: number = 0;

  public plateNumberForm = new FormControl('');
  public maxCapacityForm = new FormControl('');

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
      console.log(data)
    });

    this.inmatriculareService.getMaxCapacity().subscribe((maxCapacity) => {
      console.log(maxCapacity);
      this.maxCapacity = maxCapacity;
    });
  }

  public onSubmitPlateNumber() {
    if (this.plateNumberForm.value == '') {
      this.alertMessageEmpty();
      return;
    }
    else {
      console.log(this.plateNumberForm.value);
      this.inmatriculareService.post(this.plateNumberForm.value)
    }
  }

  public onSubmitMaxCapacity() {
    if (this.maxCapacityForm.value == '') {
      this.alertMessageEmpty();
      return;
    }
    else {
      if (this.maxCapacityForm.value != null) {
        const maxCapValue = parseInt(this.maxCapacityForm.value);
        if (isNaN(maxCapValue) || maxCapValue < 1 || maxCapValue > 999) {
          this.alertMessageInvalid();
          return;
        }
      }
      console.log(this.maxCapacityForm.value);
      this.inmatriculareService.updateMaxCapacity(this.maxCapacityForm.value);
    }
  }

  deletePlateNumber(nr: any) {
    this.inmatriculareService.deleteNumarInmatriculare(nr);
  }

  public alertMessageEmpty() {
    alert("Operation aborted: field was empty!");
  }

  public alertMessageInvalid() {
    alert("Operation aborted: field was invalid!");
  }
}