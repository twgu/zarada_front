"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

import Image from "next/image";
import img_intro from "@/public/images/intro.png";

export default function App() {
  const router = useRouter();

  useEffect(() => {
    setTimeout(() => {
      localStorage.clear();
      
      router.push(`/pages/login`, { scroll: false });
    }, 1500);
  }, []);

  return (
    <div className="login-template">
      <div className="intro-page">
        <div className="intro-character">
          <Image src={img_intro} alt="intro" priority />
        </div>
        <p className="intro-copyright">
          Copyright 2023. DAI inc. all rights reserved.
        </p>
      </div>
    </div>
  );
}
