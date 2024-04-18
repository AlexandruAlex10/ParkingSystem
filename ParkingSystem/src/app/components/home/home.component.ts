import {Component, OnInit} from '@angular/core';
import {FormControl, Validators, ɵFormGroupValue, ɵTypedOrUntyped} from "@angular/forms";
import {finalize, map} from "rxjs";
import firebase from "firebase/compat";
import { getDatabase, ref, push, set } from "firebase/database";
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
  constructor(private inmatriculareService: InmatriculareService){
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
  }
    public nrInmatriculare = new FormControl('', Validators.required)
    public maxCap = new FormControl('', Validators.required)


  public onSubmit() {
    console.log(this.nrInmatriculare.value);
    this.inmatriculareService.post(this.nrInmatriculare.value)

  }

  public onSubmitCap() {
    console.log(this.nrInmatriculare.value);
    this.inmatriculareService.postNumber(this.maxCap.value);

  }

  deleteNumar(nr : any) {
    this.inmatriculareService.deleteNumarInmatriculare(nr);
  }
}