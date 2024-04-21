import { Injectable } from '@angular/core';
import { AngularFireDatabase, AngularFireList } from "@angular/fire/compat/database";
import { getDatabase, push, ref, set, onValue, update, orderByChild, query, get, equalTo } from "firebase/database";
import { plateNumberModel } from "../models/plateNumber.model";
import { doc, deleteDoc } from "firebase/firestore";
import { Firestore } from "@angular/fire/firestore";
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class InmatriculareService {

  private dbPathPlateNumbers = '/nrInmatriculare';
  private dbPathMaxCapacity = '/nrLocuri'
  private dbPathBarrierState = '/barrierState'

  inmatriculare: AngularFireList<plateNumberModel>;
  maxCapacity: number = 0;
  openBarrierIndefinitely: boolean = false;
  closeBarrierIndefinitely: boolean = false;

  constructor(private db: AngularFireDatabase) {
    this.inmatriculare = db.list(this.dbPathPlateNumbers);
  }

  getMaxCapacity(): Observable<number> {
    const db = getDatabase();
    const starCountRef = ref(db, this.dbPathMaxCapacity);
    return new Observable<number>((observer) => {
      onValue(starCountRef, (snapshot) => {
        const data = snapshot.val();
        observer.next(data.maxCapacity);
      });
    });
  }

  getAllPlateNumbers(): AngularFireList<plateNumberModel> {
    return this.inmatriculare;
  }

  postNewPlateNumber(plateNumber: string | null) {
    const db = getDatabase();
    const postListRef = ref(db, this.dbPathPlateNumbers);
    const queryRef = query(postListRef, orderByChild('nrInmatriculare'), equalTo(plateNumber));
    const existingRecords = get(queryRef).then((snapshot) => {
      if (!snapshot.exists()) {
        const newPostRef = push(postListRef);
        set(newPostRef, {
          nrInmatriculare: plateNumber,
        });
      }
      else {
        this.alertMessageDuplicateItem(plateNumber);
      }
    })
  }

  updateMaxCapacity(nr: string | null) {
    const db = getDatabase();
    const postListRef = ref(db, this.dbPathMaxCapacity);
    const updates: Record<string, any> = {};
    updates['maxCapacity'] = nr;
    update(postListRef, updates);
  }

  updateOpenBarrierIndefinitely(barrierState: boolean | null) {
    const db = getDatabase();
    const postListRef = ref(db, this.dbPathBarrierState);
    const updates: Record<string, any> = {};
    updates['openBarrierIndefinitely'] = barrierState;
    update(postListRef, updates);
  }

  getOpenBarrierState(): Observable<boolean> {
    const db = getDatabase();
    const starCountRef = ref(db, this.dbPathBarrierState);
    return new Observable<boolean>((observer) => {
      onValue(starCountRef, (snapshot) => {
        const data = snapshot.val();
        observer.next(data.openBarrierIndefinitely);
      });
    });
  }

  updateCloseBarrierIndefinitely(barrierState: boolean | null) {
    const db = getDatabase();
    const postListRef = ref(db, this.dbPathBarrierState);
    const updates: Record<string, any> = {};
    updates['closeBarrierIndefinitely'] = barrierState;
    update(postListRef, updates);
  }

  getCloseBarrierState(): Observable<boolean> {
    const db = getDatabase();
    const starCountRef = ref(db, this.dbPathBarrierState);
    return new Observable<boolean>((observer) => {
      onValue(starCountRef, (snapshot) => {
        const data = snapshot.val();
        observer.next(data.closeBarrierIndefinitely);
      });
    });
  }

  async deleteNumarInmatriculare(nr: any) {
    const db = getDatabase();
    await deleteDoc(doc(<Firestore><unknown>db, this.dbPathPlateNumbers, nr.value));
  }

  public alertMessageDuplicateItem(plateNumber: string | null) {
    alert("Operation aborted: plate number " + plateNumber + " already exists!");
  }

}