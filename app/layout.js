import "./css/globals.css";
import "./css/reset.css";
import "./css/style.scss";

export const metadata = {
  title: "ZARADA",
  description: "Generated by twgu",
  icons: { icon: "images/hdpi.png" },
};

export default function RootLayout({ children }) {
  return (
    <html>
      <body>{children}</body>
    </html>
  );
}
