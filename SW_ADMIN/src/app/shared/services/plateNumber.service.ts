import { Injectable } from '@angular/core';
import { AngularFireDatabase, AngularFireList } from "@angular/fire/compat/database";
import { getDatabase, push, ref, set, onValue, update, orderByChild, query, get, equalTo, child, remove } from "firebase/database";
import { plateNumberModel } from "../models/plateNumber.model";
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class PlateNumberService {

  private dbPathPlateNumbers = '/plateNumbers';
  private dbPathCurrentCapacity = '/currentCapacity';
  private dbPathMaxCapacity = '/maxCapacity';
  private dbPathBarrierStateOpen = '/openBarrierIndefinitely';
  private dbPathBarrierStateClosed = '/closeBarrierIndefinitely';

  plateNumbers: AngularFireList<plateNumberModel>;
  currentCapacity: number = 0;
  maxCapacity: number = 0;
  openBarrierIndefinitely: boolean = false;
  closeBarrierIndefinitely: boolean = false;

  constructor(private db: AngularFireDatabase) {
    this.plateNumbers = db.list(this.dbPathPlateNumbers);
  }

  getCurrentCapacity(): Observable<number> {
    const db = getDatabase();
    const getCounterRef = ref(db, this.dbPathCurrentCapacity);
    return new Observable<number>((observer) => {
      onValue(getCounterRef, (snapshot) => {
        const data = snapshot.val();
        observer.next(data.currentCapacity);
      });
    });
  }

  getMaxCapacity(): Observable<number> {
    const db = getDatabase();
    const getCounterRef = ref(db, this.dbPathMaxCapacity);
    return new Observable<number>((observer) => {
      onValue(getCounterRef, (snapshot) => {
        const data = snapshot.val();
        observer.next(data.maxCapacity);
      });
    });
  }

  getAllPlateNumbers(): AngularFireList<plateNumberModel> {
    return this.plateNumbers;
  }

  postNewPlateNumber(plateNumber: string | null, isPermanent: boolean) {
    const db = getDatabase();
    const postListRef = ref(db, this.dbPathPlateNumbers);
    const queryRef = query(postListRef, orderByChild('plateNumbers'), equalTo(plateNumber));
    get(queryRef).then((snapshot) => {
      if (!snapshot.exists()) {
        const newPostRef = push(postListRef);
        set(newPostRef, {
          plateNumber: plateNumber,
          isPermanent: isPermanent,
        });
      }
      else {
        this.alertMessageDuplicateItem(plateNumber);
      }
    })
  }

  updateMaxCapacity(nr: string | null) {
    const db = getDatabase();
    const updateCounterRef = ref(db, this.dbPathMaxCapacity);
    const updates: Record<string, any> = {};
    updates['maxCapacity'] = nr;
    update(updateCounterRef, updates);
  }

  updateOpenBarrierIndefinitely(barrierState: boolean | null) {
    const db = getDatabase();
    const updateFlagRef = ref(db, this.dbPathBarrierStateOpen);
    const updates: Record<string, any> = {};
    updates['openBarrierIndefinitely'] = barrierState;
    update(updateFlagRef, updates);
  }

  getOpenBarrierState(): Observable<boolean> {
    const db = getDatabase();
    const getFlagRef = ref(db, this.dbPathBarrierStateOpen);
    return new Observable<boolean>((observer) => {
      onValue(getFlagRef, (snapshot) => {
        const data = snapshot.val();
        observer.next(data.openBarrierIndefinitely);
      });
    });
  }

  updateCloseBarrierIndefinitely(barrierState: boolean | null) {
    const db = getDatabase();
    const updateFlagRef = ref(db, this.dbPathBarrierStateClosed);
    const updates: Record<string, any> = {};
    updates['closeBarrierIndefinitely'] = barrierState;
    update(updateFlagRef, updates);
  }

  getCloseBarrierState(): Observable<boolean> {
    const db = getDatabase();
    const getFlagRef = ref(db, this.dbPathBarrierStateClosed);
    return new Observable<boolean>((observer) => {
      onValue(getFlagRef, (snapshot) => {
        const data = snapshot.val();
        observer.next(data.closeBarrierIndefinitely);
      });
    });
  }

  deletePlateNumber(plateNumber: string) {
    const db = getDatabase();
    const deleteListRef = ref(db, this.dbPathPlateNumbers);
    const queryRef = query(deleteListRef, orderByChild('plateNumber'), equalTo(plateNumber));
    get(queryRef).then((snapshot) => {
      if (snapshot.exists()) {
        const key = Object.keys(snapshot.val())[0];
        const plateToDeleteRef = child(deleteListRef, key);
        remove(plateToDeleteRef);
        this.alertMessagePlateDeletedSuccessfully(plateNumber);
      }
      else {
        this.alertMessagePlateNotFound(plateNumber);
      }
    })
  }

  public alertMessageDuplicateItem(plateNumber: string | null) {
    alert("Operation aborted: plate number " + plateNumber + " already exists!");
  }

  public alertMessagePlateDeletedSuccessfully(plateNumber: string | null) {
    alert("Plate number " + plateNumber + " was removed!");
  }

  public alertMessagePlateNotFound(plateNumber: string | null) {
    alert("Operation aborted: plate number " + plateNumber + " not found!");
  }

}