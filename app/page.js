"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function App() {
  const router = useRouter();

  useEffect(() => {
    setTimeout(() => {
      router.push(`/pages/login/`, { scroll: false });
    }, 1500);
  }, []);

  return (
    <div className="login-template">
      <div className="intro-page">
        <div className="intro-character">
          <img src="./images/intro.png" title="intro" alt="intro" />
        </div>
        <p className="intro-copyright">
          Copyright 2023. DAI inc. all rights reserved.
        </p>
      </div>
    </div>
  );
}
