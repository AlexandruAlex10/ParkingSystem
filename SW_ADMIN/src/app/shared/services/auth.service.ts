import { Injectable, NgZone } from '@angular/core';
import { UserModel } from '../models/user.model';
import * as auth from 'firebase/auth';
import { AngularFireAuth } from '@angular/fire/compat/auth';
import { AngularFirestore, AngularFirestoreDocument, DocumentSnapshot } from '@angular/fire/compat/firestore';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  userData: any;

  constructor(
    public afs: AngularFirestore,
    public afAuth: AngularFireAuth,
    public router: Router,
    public ngZone: NgZone
  ) {
    this.afAuth.authState.subscribe((user) => {
      if (user) {
        this.userData = user;
        localStorage.setItem('user', JSON.stringify(this.userData));
        JSON.parse(localStorage.getItem('user')!);
        if (user.emailVerified) {
          this.updateEmailVerificationStatusInDatabase(user.uid);
        }
      } else {
        localStorage.setItem('user', 'null');
        JSON.parse(localStorage.getItem('user')!);
      }
    });
  }

  SignIn(email: string, password: string) {
    return this.afAuth
      .signInWithEmailAndPassword(email, password)
      .then(() => {
        this.afAuth.authState.subscribe((user) => {
          if (user) {
            this.checkUserExistsInDatabase(user.uid);
          }
        });
      })
      .catch((error) => {
        window.alert(error.message);
      });
  }

  SignUp(email: string, password: string) {
    return this.afAuth
      .createUserWithEmailAndPassword(email, password)
      .then((result) => {
        this.SetUserDataAdminInDatabase(result.user);
        this.SetUserDataClientInDatabase(result.user);
        this.SendVerificationMail();
      })
      .catch((error) => {
        window.alert(error.message);
      });
  }

  SendVerificationMail() {
    return this.afAuth.currentUser
      .then((u: any) => u.sendEmailVerification())
      .then(() => {
        this.router.navigate(['verify-email']);
      });
  }

  ForgotPassword(passwordResetEmail: string) {
    return this.afAuth
      .sendPasswordResetEmail(passwordResetEmail)
      .then(() => {
        window.alert('Password reset email sent, check your inbox.');
      })
      .catch((error) => {
        window.alert(error);
      });
  }

  get isLoggedIn(): boolean {
    const user = JSON.parse(localStorage.getItem('user')!);
    return user !== null && user.emailVerified !== false ? true : false;
  }

  SignOut() {
    return this.afAuth.signOut().then(() => {
      localStorage.removeItem('user');
      this.router.navigate(['sign-in']);
    });
  }

  updateEmailVerificationStatusInDatabase(uid: string) {
    this.afs.collection('admin').doc(uid).get().subscribe(doc => {
      if (doc.exists) {
        this.afs.collection('admin').doc(uid).update({ emailVerified: true })
      }
    });
  }

  checkUserExistsInDatabase(uid: string) {
    this.afs.collection('admin').doc(uid).get().subscribe(doc => {
      if (!doc.exists) {
        this.alertMessageUnauthorizedUser();
      } else {
        this.checkEmailVerifiedInDatabase(uid)
      }
    });
  }

  checkEmailVerifiedInDatabase(uid: string) {
    this.afs.collection('admin').doc<any>(uid).get().subscribe((doc: DocumentSnapshot<any>) => {
      if (doc.exists) {
        const data = doc.data() as { emailVerified: boolean };
        const emailVerified = data.emailVerified;
        if (emailVerified) {
          this.router.navigate(['dashboard']);
        } else {
          this.alertMessageUnauthorizedUser();
        }
      } else {
        this.alertMessageUnauthorizedUser();
      }
    });
  }  

  SetUserDataAdminInDatabase(user: any) {
    const userRef: AngularFirestoreDocument<any> = this.afs.doc(
      `admin/${user.uid}`
    );
    const userData: UserModel = {
      uid: user.uid,
      email: user.email,
      photoURL: user.photoURL,
      emailVerified: user.emailVerified,
    };
    return userRef.set(userData, {
      merge: true,
    });
  }

  SetUserDataClientInDatabase(user: any) {
    const userRef: AngularFirestoreDocument<any> = this.afs.doc(
      `client/${user.uid}`
    );
    const userData: UserModel = {
      uid: user.uid,
      email: user.email,
    };
    return userRef.set(userData, {
      merge: true,
    });
  }

  public alertMessageUnauthorizedUser() {
    window.alert("Please wait until an admin activates your account.")
  }
}
