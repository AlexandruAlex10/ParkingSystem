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

  private dbPath = '/nrInmatriculare';
  private dbPathMax = '/nrLocuri'
  inmatriculare: AngularFireList<plateNumberModel>;
  maxCapacity: number = 0;

  constructor(private db: AngularFireDatabase) {
    this.inmatriculare = db.list(this.dbPath);
  }

  getMaxCapacity(): Observable<number> {
    const db = getDatabase();
    const starCountRef = ref(db, this.dbPathMax);
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
    const postListRef = ref(db, this.dbPath);
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
    const postListRef = ref(db, this.dbPathMax);
    const updates: Record<string, any> = {};
    updates['maxCapacity'] = nr;
    update(postListRef, updates);
  }

  async deleteNumarInmatriculare(nr: any) {
    const db = getDatabase();
    await deleteDoc(doc(<Firestore><unknown>db, this.dbPath, nr.value));
  }

  public alertMessageDuplicateItem(plateNumber: string | null) {
    alert("Operation aborted: plate number " + plateNumber + " already exists!");
  }  

}