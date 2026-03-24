using UnityEngine;
using UnityEngine.EventSystems;

namespace ProjectRelay.UI
{
    public class HoldButtonInput : MonoBehaviour, IPointerDownHandler, IPointerUpHandler
    {
        public bool IsHolding { get; private set; }

        public void OnPointerDown(PointerEventData eventData)
        {
            IsHolding = true;
        }

        public void OnPointerUp(PointerEventData eventData)
        {
            IsHolding = false;
        }

        private void OnDisable()
        {
            IsHolding = false;
        }
    }
}
