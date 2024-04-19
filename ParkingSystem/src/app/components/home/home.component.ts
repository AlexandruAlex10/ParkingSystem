import {Component, OnInit} from '@angular/core';
import {FormControl, Validators, ɵFormGroupValue, ɵTypedOrUntyped} from "@angular/forms";
import {map} from "rxjs";
import {InmatriculareService} from "../../shared/services/inmatriculare.service";
import {inmatriculareModel} from "../../shared/models/inmatriculare.model";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit{
  public nrInmat: inmatriculareModel[]= [];
  public maxCapacity: number = 0;
  constructor(protected inmatriculareService: InmatriculareService){
  }
  ngOnInit() {
    this.inmatriculareService.getAll()!.snapshotChanges().pipe(
      map(changes =>
        changes.map(c =>
          ({ key: c.payload.key, ...c.payload.val() })
        )
      )
    ).subscribe(data => {
      this.nrInmat = data;
      console.log(data)
    });

    this.inmatriculareService.getMaxCapacity().subscribe((maxCapacity) => {
      console.log('Max Capacity:', maxCapacity);
      this.maxCapacity = maxCapacity;
    });
  }
    public nrInmatriculare = new FormControl('', Validators.required)
    public maxCap = new FormControl('', Validators.required)


  public onSubmit() {
    console.log(this.nrInmatriculare.value);
    this.inmatriculareService.post(this.nrInmatriculare.value)
  }

  public onSubmitCap() {
    console.log(this.nrInmatriculare.value);
    this.inmatriculareService.updateNumber(this.maxCap.value);
  }

  deleteNumar(nr : any) {
    this.inmatriculareService.deleteNumarInmatriculare(nr);
  }
}