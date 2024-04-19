import { Injectable } from '@angular/core';
import {AngularFireDatabase, AngularFireList} from "@angular/fire/compat/database";
import {getDatabase, push, ref, set,onValue, update} from "firebase/database";
import {inmatriculareModel} from "../models/inmatriculare.model";
import { doc, deleteDoc } from "firebase/firestore";
import {Firestore} from "@angular/fire/firestore";
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class InmatriculareService {

  private dbPath = '/nrInmatriculare';
  private dbPathMax = '/nrLocuri'
  inmatriculare: AngularFireList<inmatriculareModel>;
  maxCapacity: number=0;

  constructor(private db: AngularFireDatabase) {
    this.inmatriculare = db.list(this.dbPath);
    console.log(db.list(this.dbPath));
  }

  getNr():number{
    const db = getDatabase();
    const starCountRef = ref(db, this.dbPathMax);
    onValue(starCountRef, (snapshot) => {
      const data = snapshot.val();
      this.maxCapacity=data;
    });
    return this.maxCapacity;
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

  getAll(): AngularFireList<inmatriculareModel> {
    return this.inmatriculare;
  }

  post(numar: string | null){
    const db = getDatabase();
    const postListRef = ref(db, this.dbPath);
    const newPostRef = push(postListRef);
    set(newPostRef, {
      nrInmatriculare: numar,
    });
  }

  updateNumber(nr: string | null){
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

}