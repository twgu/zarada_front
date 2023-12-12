"use client";

import axios from "axios";
import { useRef } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import Image from "next/image";
import img_logoLogin from "@/public/images/logo-login.png";

export default function Login() {
  const router = useRouter();

  const idRef = useRef();
  const pwRef = useRef();

  const login = () => {
    axios
      .post(process.env.NEXT_PUBLIC_API_URL + "/Login", {
        userId: idRef.current.value,
        password: pwRef.current.value,
        auto_login: "N",
        deviceType: "0",
      })
      .then((response) => {
        if (response.data == "") {
          alert("없는 아이디입니다.");
          return;
        }

        if (response.data.pw != response.data.enpassword) {
          alert("비밀번호입력이 잘 못 되었습니다.");
          return;
        }

        if (
          response.data.userStat == "T" ||
          response.data.userStat == "N" ||
          response.data.userStat == "X" ||
          response.data.userStat == "C"
        ) {
          alert("간호사 사용자 확인 이후 사용가능합니다.");
          return;
        }

        // 사용자 구분에 따라 라우팅
        if (response.data.userCl == "N") {
          if (response.data.loginCount == 0) {
            router.push(`/pages/login/introSlide/`, { scroll: false });
          } else {
            router.push(`/pages/home/user/`, { scroll: false });
          }
        } else {
          router.push(`/pages/home/admin/`, { scroll: false });
        }
      });
  };

  return (
    <div className="login-template">
      <div className="login-page">
        <h2 className="login-tit">
          <Image src={img_logoLogin} alt="logo-login" priority />
        </h2>
        <div className="login-form">
          <div className="input-area">
            <input
              type="text"
              id="login-id"
              className="default-input"
              placeholder="아이디"
              ref={idRef}
            />
            <label htmlFor="login-id" className="blind">
              아이디
            </label>
          </div>
          <div className="input-area">
            <input
              type="password"
              id="login-pwd"
              className="default-input"
              placeholder="비밀번호"
              ref={pwRef}
            />
            <label htmlFor="login-pwd" className="blind">
              비밀번호
            </label>
          </div>
          <div className="login-chk-area">
            <input
              type="checkbox"
              id="login-chk-first"
              name="login-chk"
              className="default-chk"
            />
            <label htmlFor="login-chk-first" className="default-label">
              <span className="default-label-txt"></span>아이디 저장
            </label>
            <input
              type="checkbox"
              id="login-chk-second"
              name="login-chk"
              className="default-chk"
            />
            <label
              htmlFor="login-chk-second"
              className="default-label auto-login"
            >
              <span className="default-label-txt"></span>자동 로그인
            </label>
          </div>
          <div className="login-btn-area">
            <button className="default-btn join-btn" onClick={login}>
              LOGIN
            </button>
          </div>
        </div>
        <div className="login-link-area">
          <Link
            href="/pages/join/membershipIdFind/"
            className="login-link"
            scroll={false}
          >
            아이디 찾기
          </Link>
          <Link
            href="/pages/join/membershipPwFind/"
            className="login-link center"
            scroll={false}
          >
            비밀번호 찾기
          </Link>
          <Link
            href="/pages/join/membershipJoin/first/"
            className="login-link"
            scroll={false}
          >
            회원가입
          </Link>
        </div>
        <p className="copyright">
          Copyright 2023. DAI inc. all rights reserved.
        </p>
        <span className="login-foot-img"></span>
      </div>
    </div>
  );
}
