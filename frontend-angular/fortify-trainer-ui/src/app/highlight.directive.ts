import { Directive, ElementRef, AfterViewInit } from '@angular/core';
import hljs from 'highlight.js';

@Directive({
  selector: '[highlight]'  // ← uso como atributo: <div highlight>
})
export class HighlightDirective implements AfterViewInit {
  constructor(private el: ElementRef) {}

  ngAfterViewInit(): void {
    const blocks = this.el.nativeElement.querySelectorAll('pre code');

    blocks.forEach((block: HTMLElement) => {
      hljs.highlightElement(block);  // aplica o highlight
    });
  }
}
