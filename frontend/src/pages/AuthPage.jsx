// src/features/auth/AuthPage.jsx
import { useState } from "react";
import { useAuth } from "@app/AuthContext";
import clsx from "clsx";
import auth from "@assets/css/AuthPage.module.css";
import { FaFacebookF, FaGooglePlusG, FaLinkedinIn } from "react-icons/fa";

export default function AuthPage() {
  const [isSignUp, setIsSignUp] = useState(false);
  const [name, setName] = useState("");
  const [pass, setPass] = useState("");

  const { signIn, signUp } = useAuth();

  const handleSignIn = (e) => {
    e.preventDefault();
    signIn(name, pass).catch(console.error);
  };

  const handleSignUp = (e) => {
    e.preventDefault();
    signUp(name, pass).catch(console.error);
  };

  return (
    <div className={clsx(auth.container, isSignUp && auth.rightPanelActive)}>
      {/* Регистрация */}
      <div className={clsx(auth.formContainer, auth.signUpContainer)}>
        <form onSubmit={handleSignUp} className={auth.form}>
          <h1 className={auth.title}>Создать аккаунт</h1>
          <SocialRow />
          <span className={auth.caption}>
            или используйте вашу почту для регистрации
          </span>
          <input
            type="text"
            placeholder="Никнейм"
            required
            className={auth.input}
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          <input
            type="password"
            placeholder="Пароль"
            required
            className={auth.input}
            value={pass}
            onChange={(e) => setPass(e.target.value)}
          />
          <button className={auth.btn}>Зарегистрироваться</button>
        </form>
      </div>

      {/* Вход */}
      <div className={clsx(auth.formContainer, auth.signInContainer)}>
        <form onSubmit={handleSignIn} className={auth.form}>
          <h1 className={auth.title}>Войти</h1>
          <SocialRow />
          <span className={auth.caption}>или используйте ваш аккаунт</span>
          <input
            type="text"
            placeholder="Никнейм"
            required
            className={auth.input}
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          <input
            type="password"
            placeholder="Пароль"
            required
            className={auth.input}
            value={pass}
            onChange={(e) => setPass(e.target.value)}
          />
          <a href="#" className={auth.link}>
            Забыли пароль?
          </a>
          <button className={auth.btn}>Войти</button>
        </form>
      </div>

      <div className={auth.overlayContainer}>
        <div className={auth.overlay}>
          <div className={clsx(auth.overlayPanel, auth.overlayLeft)}>
            <h1 className={auth.title}>С возвращением!</h1>
            <p className={auth.paragraph}>
              Чтобы оставаться на связи, пожалуйста, войдите используя свои данные
            </p>
            <button
              className={clsx(auth.btn, auth.ghost)}
              onClick={() => setIsSignUp(false)}
            >
              Войти
            </button>
          </div>
          <div className={clsx(auth.overlayPanel, auth.overlayRight)}>
            <h1 className={auth.title}>Привет, друг!</h1>
            <p className={auth.paragraph}>
              Введите свои данные, чтобы начать работу с нами
            </p>
            <button
              className={clsx(auth.btn, auth.ghost)}
              onClick={() => setIsSignUp(true)}
            >
              Зарегистрироваться
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function SocialRow() {
  return (
    <div className={auth.socialRow}>
      <a href="#" className={auth.social}>
        <FaFacebookF />
      </a>
      <a href="#" className={auth.social}>
        <FaGooglePlusG />
      </a>
      <a href="#" className={auth.social}>
        <FaLinkedinIn />
      </a>
    </div>
  );
}
