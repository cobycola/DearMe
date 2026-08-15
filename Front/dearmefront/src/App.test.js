import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders DearMe brand", () => {
  render(<App />);
  expect(screen.getByText(/DearMe/i)).toBeInTheDocument();
});